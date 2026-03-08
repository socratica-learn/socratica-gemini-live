# Cloud Run Deployment

## Backend service

The backend can be deployed to Google Cloud Run with:

```powershell
pwsh ./scripts/deploy-backend-cloud-run.ps1 `
  -ProjectId "your-gcp-project-id" `
  -GeminiApiKey "your-gemini-api-key" `
  -MongoUri "your-mongodb-connection-string" `
  -FrontendUrl "https://your-frontend-domain" `
  -CorsAllowedOrigins "https://your-frontend-domain"
```

This script:

1. Enables the required Google Cloud APIs.
2. Creates an Artifact Registry repository named `socratica`.
3. Builds the backend image with Cloud Build.
4. Deploys the image to Cloud Run.

## Required environment values

- `GEMINI_API_KEY`
- `SPRING_DATA_MONGODB_URI`
- `MONGODB_DATABASE`
- `JWT_SECRET`
- `FRONTEND_URL`
- `CORS_ALLOWED_ORIGINS`

## Hackathon proof points

- Cloud Run satisfies the Google Cloud hosting requirement.
- `cloudbuild.backend.yaml` provides scripted deployment for the infrastructure automation bonus.
- The live voice token endpoint is exposed by the deployed backend and can be cited in the repository as proof of Google Cloud usage.
