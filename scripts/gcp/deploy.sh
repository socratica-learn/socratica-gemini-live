#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TERRAFORM_DIR="${ROOT_DIR}/infra/terraform"
PROJECT_ID="${PROJECT_ID:-project-8d21f1f6-2009-4dcf-bff}"
ORG_ID="${ORG_ID:-809804464459}"
REGION="${REGION:-europe-west4}"

require_command() {
  local command_name="$1"
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "Missing required command: ${command_name}" >&2
    exit 1
  fi
}

check_gcloud_user_auth() {
  gcloud auth print-access-token >/dev/null 2>&1
}

check_gcloud_adc_auth() {
  gcloud auth application-default print-access-token >/dev/null 2>&1
}

terraform_state_has() {
  local address="$1"
  terraform -chdir="${TERRAFORM_DIR}" state show "${address}" >/dev/null 2>&1
}

terraform_import_if_untracked() {
  local address="$1"
  local import_id="$2"

  if terraform_state_has "${address}"; then
    return 0
  fi

  if terraform -chdir="${TERRAFORM_DIR}" import \
    -var="organization_id=${ORG_ID}" \
    -var="project_id=${PROJECT_ID}" \
    -var="region=${REGION}" \
    -var="deploy_services=false" \
    "${address}" "${import_id}" >/dev/null 2>&1; then
    echo "Imported existing Terraform resource: ${address}"
  fi
}

ensure_adc_quota_project() {
  if ! gcloud auth application-default set-quota-project "${PROJECT_ID}" >/dev/null 2>&1; then
    echo "Failed to set the ADC quota project to ${PROJECT_ID}." >&2
    echo "Run: gcloud auth application-default set-quota-project ${PROJECT_ID}" >&2
    exit 1
  fi
}

require_command gcloud
require_command terraform
require_command python3

if command -v git >/dev/null 2>&1 && git -C "${ROOT_DIR}" rev-parse --short HEAD >/dev/null 2>&1; then
  IMAGE_TAG="${IMAGE_TAG:-$(git -C "${ROOT_DIR}" rev-parse --short HEAD)}"
else
  IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
fi

if ! check_gcloud_user_auth; then
  echo "Active gcloud CLI credentials are not usable for deployment." >&2
  echo "Run: gcloud auth login --update-adc" >&2
  exit 1
fi

if ! check_gcloud_adc_auth; then
  echo "Application Default Credentials are not usable for Terraform." >&2
  echo "Run: gcloud auth login --update-adc" >&2
  exit 1
fi

export GOOGLE_CLOUD_QUOTA_PROJECT="${PROJECT_ID}"
ensure_adc_quota_project

PROJECT_NUMBER="$(gcloud projects describe "${PROJECT_ID}" --format='value(projectNumber)')"
ARTIFACT_REGISTRY_REPOSITORY="socratica"
BACKEND_SERVICE_ACCOUNT_EMAIL="socratica-backend@${PROJECT_ID}.iam.gserviceaccount.com"
FRONTEND_SERVICE_ACCOUNT_EMAIL="socratica-frontend@${PROJECT_ID}.iam.gserviceaccount.com"
SECRET_IMPORT_KEYS=(
  "mongodb_uri:socratica-mongodb-uri"
  "jwt_secret:socratica-jwt-secret"
  "google_client_id:socratica-google-client-id"
  "google_client_secret:socratica-google-client-secret"
  "mail_username:socratica-mail-username"
  "mail_password:socratica-mail-password"
)
BACKEND_ROLE_IMPORTS=(
  "roles/aiplatform.user"
  "roles/secretmanager.secretAccessor"
)
BUILD_ROLE_IMPORTS=(
  "serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com|roles/artifactregistry.writer"
  "serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com|roles/logging.logWriter"
  "serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com|roles/storage.admin"
  "serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com|roles/artifactregistry.writer"
  "serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com|roles/logging.logWriter"
  "serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com|roles/storage.admin"
)

BOOTSTRAP_TARGETS=(
  'data.google_project.current'
  'terraform_data.project_org_guardrail'
  'google_project_service.required'
  'google_artifact_registry_repository.containers'
  'google_service_account.backend'
  'google_service_account.frontend'
  'google_project_iam_member.backend_roles'
  'google_project_iam_member.build_roles'
  'google_secret_manager_secret.app'
)

terraform -chdir="${TERRAFORM_DIR}" init

terraform_import_if_untracked \
  'google_artifact_registry_repository.containers' \
  "projects/${PROJECT_ID}/locations/${REGION}/repositories/${ARTIFACT_REGISTRY_REPOSITORY}"
terraform_import_if_untracked \
  'google_service_account.backend' \
  "projects/${PROJECT_ID}/serviceAccounts/${BACKEND_SERVICE_ACCOUNT_EMAIL}"
terraform_import_if_untracked \
  'google_service_account.frontend' \
  "projects/${PROJECT_ID}/serviceAccounts/${FRONTEND_SERVICE_ACCOUNT_EMAIL}"

for secret_entry in "${SECRET_IMPORT_KEYS[@]}"; do
  secret_key="${secret_entry%%:*}"
  secret_id="${secret_entry#*:}"
  terraform_import_if_untracked \
    "google_secret_manager_secret.app[\"${secret_key}\"]" \
    "projects/${PROJECT_ID}/secrets/${secret_id}"
done

for role in "${BACKEND_ROLE_IMPORTS[@]}"; do
  terraform_import_if_untracked \
    "google_project_iam_member.backend_roles[\"${role}\"]" \
    "${PROJECT_ID} ${role} serviceAccount:${BACKEND_SERVICE_ACCOUNT_EMAIL}"
done

for build_role_entry in "${BUILD_ROLE_IMPORTS[@]}"; do
  build_member="${build_role_entry%%|*}"
  build_role="${build_role_entry#*|}"
  terraform_import_if_untracked \
    "google_project_iam_member.build_roles[\"${build_member}|${build_role}\"]" \
    "${PROJECT_ID} ${build_role} ${build_member}"
done

terraform -chdir="${TERRAFORM_DIR}" apply -auto-approve \
  "${BOOTSTRAP_TARGETS[@]/#/-target=}" \
  -var="organization_id=${ORG_ID}" \
  -var="project_id=${PROJECT_ID}" \
  -var="region=${REGION}" \
  -var="deploy_services=false"

PROJECT_ID="${PROJECT_ID}" \
"${ROOT_DIR}/scripts/gcp/sync-secrets.sh"

AR_REPOSITORY="$(terraform -chdir="${TERRAFORM_DIR}" output -raw artifact_registry_repository)"

gcloud builds submit "${ROOT_DIR}" \
  --project "${PROJECT_ID}" \
  --region "${REGION}" \
  --config "${ROOT_DIR}/cloudbuild.yaml" \
  --substitutions "_REGION=${REGION},_AR_REPOSITORY=${AR_REPOSITORY},_IMAGE_TAG=${IMAGE_TAG}"

BACKEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPOSITORY}/backend:${IMAGE_TAG}"
FRONTEND_IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPOSITORY}/frontend:${IMAGE_TAG}"

terraform -chdir="${TERRAFORM_DIR}" apply -auto-approve \
  -var="organization_id=${ORG_ID}" \
  -var="project_id=${PROJECT_ID}" \
  -var="region=${REGION}" \
  -var="deploy_services=true" \
  -var="backend_image=${BACKEND_IMAGE}" \
  -var="frontend_image=${FRONTEND_IMAGE}"

BACKEND_URL="$(terraform -chdir="${TERRAFORM_DIR}" output -raw backend_url)"
FRONTEND_URL="$(terraform -chdir="${TERRAFORM_DIR}" output -raw frontend_url)"

terraform -chdir="${TERRAFORM_DIR}" apply -auto-approve \
  -var="organization_id=${ORG_ID}" \
  -var="project_id=${PROJECT_ID}" \
  -var="region=${REGION}" \
  -var="deploy_services=true" \
  -var="backend_image=${BACKEND_IMAGE}" \
  -var="frontend_image=${FRONTEND_IMAGE}" \
  -var="backend_public_url=${BACKEND_URL}" \
  -var="frontend_public_url=${FRONTEND_URL}"

echo "Backend URL: ${BACKEND_URL}"
echo "Frontend URL: ${FRONTEND_URL}"
