# GCP Cloud Run Deployment

This repository now includes Terraform and Cloud Build assets for deploying Socratica to:

- Organization: `809804464459`
- Project: `project-8d21f1f6-2009-4dcf-bff`
- Region: `europe-west4`

## What Gets Provisioned

- Required project services and APIs
- Artifact Registry repository for backend and frontend images
- Cloud Run runtime service accounts
- IAM bindings for Secret Manager access and Cloud Build image pushes
- Secret Manager secret containers for the app configuration
- Two public Cloud Run services:
  - `socratica-backend`
  - `socratica-frontend`

## Auth Model

All Gemini traffic uses `GEMINI_API_KEY`.

- Server-side generation uses `GEMINI_API_KEY` from Secret Manager.
- Gemini Live uses the backend websocket proxy with that same API key.
- Terraform enables the Gemini API plus Secret Manager, Artifact Registry, Cloud Build, and Cloud Run services the deployment needs.

## Secrets

`scripts/gcp/sync-secrets.sh` reads the local `.env` file and writes Secret Manager versions for:

- `SPRING_DATA_MONGODB_URI`
- `JWT_SECRET`
- `GEMINI_API_KEY`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

The current workspace `.env` already contains a non-local MongoDB SRV URI, so the deployment can reuse that existing database instead of trying to replace the persistence layer during this rollout.

## Deployment

Run:

```bash
./scripts/gcp/deploy.sh
```

If your organization enforces periodic re-authentication, refresh the local Cloud SDK session first:

```bash
gcloud auth login --update-adc
```

The script performs three phases:

1. Terraform bootstrap for APIs, IAM, Artifact Registry, secret containers, and service accounts.
2. Secret sync plus Cloud Build image builds and pushes.
3. Terraform Cloud Run deployment, followed by a second apply to wire the final Cloud Run URLs back into backend CORS and OAuth settings.

## Notes

- If the Artifact Registry repository, runtime service accounts, secrets, or bootstrap IAM bindings already exist in the project, `scripts/gcp/deploy.sh` imports them into Terraform state before the first apply so reruns do not fail with `409 already exists`.
- The frontend now proxies `/api/*` and websocket traffic at runtime using `BACKEND_UPSTREAM`, so it remains same-origin from the browser on Cloud Run.
- `FRONTEND_URL` and OAuth callback URLs are finalized on the second Terraform apply because the Cloud Run URLs are not known until the first deployment finishes.
