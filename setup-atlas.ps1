# PowerShell script to set up MongoDB Atlas connection for Socratica

Write-Host ""
Write-Host "=== MongoDB Atlas Setup for Socratica ===" -ForegroundColor Cyan
Write-Host ""

Write-Host "First, you need to get your database password from MongoDB Atlas:" -ForegroundColor Yellow
Write-Host "1. Go to: https://cloud.mongodb.com/" -ForegroundColor White
Write-Host "2. Click 'Database Access' (left sidebar)" -ForegroundColor White
Write-Host "3. Find user 'msecara_db_user' and click 'Edit'" -ForegroundColor White
Write-Host "4. Click 'Edit Password' and either:" -ForegroundColor White
Write-Host "   - Enter your existing password (if you remember it)" -ForegroundColor White
Write-Host "   - Or create a new password" -ForegroundColor White
Write-Host "5. Copy the password" -ForegroundColor White
Write-Host ""
Write-Host "Press Enter when you have your password ready..." -ForegroundColor Cyan
Read-Host

Write-Host ""
Write-Host "Enter your MongoDB Atlas database password:" -ForegroundColor Yellow
$password = Read-Host "Password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

# URL encode the password (handle special characters)
$passwordEncoded = [System.Uri]::EscapeDataString($passwordPlain)

# Build connection string
$connectionString = "mongodb+srv://msecara_db_user:$passwordEncoded@cluster0.txarzpn.mongodb.net/socratica_dev?retryWrites=true&w=majority"

Write-Host ""
Write-Host "Setting environment variable..." -ForegroundColor Yellow
$env:SPRING_DATA_MONGODB_URI = $connectionString

Write-Host "✓ Environment variable set for this session!" -ForegroundColor Green
Write-Host ""

# Also update docker-compose.yml
Write-Host "Updating docker-compose.yml..." -ForegroundColor Yellow
$dockerComposePath = "docker-compose.yml"
if (Test-Path $dockerComposePath) {
    $content = Get-Content $dockerComposePath -Raw
    $content = $content -replace 'YOUR_PASSWORD', $passwordEncoded
    Set-Content $dockerComposePath $content
    Write-Host "✓ docker-compose.yml updated!" -ForegroundColor Green
} else {
    Write-Host "⚠ docker-compose.yml not found" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Connection String (masked):" -ForegroundColor Cyan
Write-Host "mongodb+srv://msecara_db_user:***@cluster0.txarzpn.mongodb.net/socratica_dev?retryWrites=true&w=majority" -ForegroundColor White
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker is running" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now start the backend with:" -ForegroundColor Cyan
    Write-Host "  docker-compose up -d backend" -ForegroundColor White
} catch {
    Write-Host "✗ Docker is not running" -ForegroundColor Red
    Write-Host ""
    Write-Host "To start the backend without Docker, run:" -ForegroundColor Cyan
    Write-Host "  cd backend" -ForegroundColor White
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
}

Write-Host ""
Write-Host "=== Setup Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Important: Make sure Network Access is configured in MongoDB Atlas:" -ForegroundColor Yellow
Write-Host "1. Go to MongoDB Atlas → Network Access" -ForegroundColor White
Write-Host "2. Click 'Add IP Address'" -ForegroundColor White
Write-Host "3. Click 'Allow Access from Anywhere' (0.0.0.0/0)" -ForegroundColor White
Write-Host "4. Click 'Confirm'" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. ✅ Configure Network Access (if not done)" -ForegroundColor White
Write-Host "2. 🎯 Start the backend: docker-compose up -d backend" -ForegroundColor White
Write-Host "3. 🎯 Connect with Navicat using:" -ForegroundColor White
Write-Host "   - Host: cluster0.txarzpn.mongodb.net" -ForegroundColor Gray
Write-Host "   - Username: msecara_db_user" -ForegroundColor Gray
Write-Host "   - Password: (the one you just entered)" -ForegroundColor Gray
Write-Host "   - Database: socratica_dev" -ForegroundColor Gray

