#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TERRAFORM_DIR="${ROOT_DIR}/infra/terraform"
ENV_FILE="${ENV_FILE:-${ROOT_DIR}/.env}"
PROJECT_ID="${PROJECT_ID:-project-8d21f1f6-2009-4dcf-bff}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing env file: ${ENV_FILE}" >&2
  exit 1
fi

set -a
source "${ENV_FILE}"
set +a

SECRET_NAMES_JSON="$(terraform -chdir="${TERRAFORM_DIR}" output -json secret_names)"

secret_name() {
  local key="$1"
  printf '%s' "${SECRET_NAMES_JSON}" | python3 -c 'import json,sys; print(json.load(sys.stdin)[sys.argv[1]])' "${key}"
}

add_secret_version() {
  local secret="$1"
  local value="$2"
  printf '%s' "${value}" | gcloud secrets versions add "${secret}" \
    --project "${PROJECT_ID}" \
    --data-file=- >/dev/null
}

add_secret_version "$(secret_name mongodb_uri)" "${SPRING_DATA_MONGODB_URI:-}"
add_secret_version "$(secret_name jwt_secret)" "${JWT_SECRET:-}"
add_secret_version "$(secret_name google_client_id)" "${GOOGLE_CLIENT_ID:-}"
add_secret_version "$(secret_name google_client_secret)" "${GOOGLE_CLIENT_SECRET:-}"
add_secret_version "$(secret_name mail_username)" "${MAIL_USERNAME:-}"
add_secret_version "$(secret_name mail_password)" "${MAIL_PASSWORD:-}"

echo "Secret sync completed for project ${PROJECT_ID}."
