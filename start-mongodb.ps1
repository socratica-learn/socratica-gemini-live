# PowerShell script to start MongoDB and verify setup

Write-Host "=== Starting MongoDB for Socratica ===" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not running. Please start Docker Desktop first!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Starting MongoDB container..." -ForegroundColor Yellow
docker-compose up -d mongodb

Write-Host ""
Write-Host "Waiting for MongoDB to be healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Checking MongoDB status..." -ForegroundColor Yellow
docker-compose ps mongodb

Write-Host ""
Write-Host "Testing MongoDB connection..." -ForegroundColor Yellow
docker exec -it socratica-mongodb mongosh --eval "db.adminCommand('ping')" --quiet

Write-Host ""
Write-Host "=== MongoDB Setup Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Connection details for Navicat:" -ForegroundColor Cyan
Write-Host "  Host: localhost" -ForegroundColor White
Write-Host "  Port: 27017" -ForegroundColor White
Write-Host "  Username: socratica_user" -ForegroundColor White
Write-Host "  Password: socratica_password" -ForegroundColor White
Write-Host "  Auth Database: admin" -ForegroundColor White
Write-Host "  Database: socratica_dev" -ForegroundColor White
Write-Host ""
Write-Host "Next step: Start the backend with: docker-compose up -d backend" -ForegroundColor Yellow




