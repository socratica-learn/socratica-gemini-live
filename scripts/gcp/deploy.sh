#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TERRAFORM_DIR="${ROOT_DIR}/infra/terraform"
PROJECT_ID="${PROJECT_ID:-project-8d21f1f6-2009-4dcf-bff}"
ORG_ID="${ORG_ID:-809804464459}"
REGION="${REGION:-europe-west4}"

if command -v git >/dev/null 2>&1 && git -C "${ROOT_DIR}" rev-parse --short HEAD >/dev/null 2>&1; then
  IMAGE_TAG="${IMAGE_TAG:-$(git -C "${ROOT_DIR}" rev-parse --short HEAD)}"
else
  IMAGE_TAG="${IMAGE_TAG:-$(date +%Y%m%d%H%M%S)}"
fi

if ! gcloud auth print-access-token >/dev/null 2>&1; then
  echo "Active gcloud credentials are not usable for deployment." >&2
  echo "Re-authenticate with: gcloud auth login --update-adc" >&2
  exit 1
fi

terraform -chdir="${TERRAFORM_DIR}" init

terraform -chdir="${TERRAFORM_DIR}" apply -auto-approve \
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
