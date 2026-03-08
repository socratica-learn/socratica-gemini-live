if (-not (Test-Path ".env")) {
    Write-Host "No .env file found. Copy .env.example to .env and fill in GEMINI_API_KEY first." -ForegroundColor Yellow
    exit 1
}

$envFile = Get-Content ".env"
$hasGeminiKey = $envFile | Where-Object { $_ -match "^GEMINI_API_KEY=" -and $_ -notmatch "^GEMINI_API_KEY=$" -and $_ -notmatch "replace-with-your-gemini-api-key" }

if (-not $hasGeminiKey) {
    Write-Host "GEMINI_API_KEY is missing in .env." -ForegroundColor Yellow
    exit 1
}

docker compose up --build mongo backend
