param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectId,

    [Parameter(Mandatory = $true)]
    [string]$GeminiApiKey,

    [Parameter(Mandatory = $true)]
    [string]$MongoUri,

    [string]$Region = "europe-west4",
    [string]$Service = "socratica-backend",
    [string]$MongoDatabase = "socratica_dev",
    [string]$GeminiLiveModel = "gemini-live-2.5-flash-preview",
    [string]$FrontendUrl = "http://localhost:5173",
    [string]$CorsAllowedOrigins = "http://localhost:5173",
    [string]$JwtSecret = "change-me"
)

$image = "$Region-docker.pkg.dev/$ProjectId/socratica/socratica-backend:latest"

gcloud config set project $ProjectId
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com

gcloud artifacts repositories create socratica `
  --repository-format=docker `
  --location=$Region `
  --description="Socratica backend images" 2>$null

gcloud builds submit . `
  --config=cloudbuild.backend.yaml `
  --substitutions=_SERVICE=$Service,_REGION=$Region,_IMAGE=$image,_SPRING_DATA_MONGODB_URI=$MongoUri,_MONGODB_DATABASE=$MongoDatabase,_GEMINI_API_KEY=$GeminiApiKey,_GEMINI_LIVE_MODEL=$GeminiLiveModel,_JWT_SECRET=$JwtSecret,_FRONTEND_URL=$FrontendUrl,_CORS_ALLOWED_ORIGINS=$CorsAllowedOrigins
