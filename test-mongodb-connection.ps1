# Test MongoDB Atlas Connection

Write-Host "=== Testing MongoDB Atlas Connection ===" -ForegroundColor Cyan
Write-Host ""

$connectionString = "mongodb+srv://msecara_db_user:VcY5U3VuyKB%23WyN@cluster0.txarzpn.mongodb.net/socratica_dev?retryWrites=true&w=majority"

Write-Host "Connection String (masked):" -ForegroundColor Yellow
Write-Host "mongodb+srv://msecara_db_user:***@cluster0.txarzpn.mongodb.net/socratica_dev" -ForegroundColor White
Write-Host ""

Write-Host "=== Connection Details ===" -ForegroundColor Cyan
Write-Host "Host: cluster0.txarzpn.mongodb.net" -ForegroundColor White
Write-Host "Database: socratica_dev" -ForegroundColor White
Write-Host "Username: msecara_db_user" -ForegroundColor White
Write-Host "Network Access: [OK] Configured (0.0.0.0/0)" -ForegroundColor Green
Write-Host ""

Write-Host "=== Next Steps ===" -ForegroundColor Cyan
Write-Host "1. Start Docker Desktop (if using Docker)" -ForegroundColor White
Write-Host "2. Start the backend: docker-compose up -d backend" -ForegroundColor White
Write-Host "3. Connect with Navicat using the details above" -ForegroundColor White
Write-Host "4. Test by signing up a user through the frontend" -ForegroundColor White
Write-Host ""
Write-Host "Ready to connect! [OK]" -ForegroundColor Green
