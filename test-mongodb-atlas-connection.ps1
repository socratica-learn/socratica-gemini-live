# Test MongoDB Atlas Connection and Diagnose Issues

Write-Host ""
Write-Host "=== MongoDB Atlas Connection Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Get current public IP
Write-Host "Step 1: Checking your public IP address..." -ForegroundColor Yellow
try {
    $publicIP = (Invoke-WebRequest -Uri "https://api.ipify.org" -UseBasicParsing).Content.Trim()
    Write-Host "✓ Your current public IP: $publicIP" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Could not determine your public IP" -ForegroundColor Red
    Write-Host "  Please check manually at: https://www.whatismyip.com/" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Step 2: MongoDB Atlas Network Access Check" -ForegroundColor Yellow
Write-Host ""
Write-Host "The SSL error you're seeing indicates your IP is NOT whitelisted in MongoDB Atlas." -ForegroundColor Red
Write-Host ""
Write-Host "ACTION REQUIRED:" -ForegroundColor Cyan
Write-Host "1. Open this link in your browser:" -ForegroundColor White
$networkAccessUrl = "https://cloud.mongodb.com/v2#/security/network/whitelist"
Write-Host "   $networkAccessUrl" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Check if you see any IP addresses listed" -ForegroundColor White
if ($publicIP) {
    Write-Host "   - Look for your IP: $publicIP" -ForegroundColor Gray
}
Write-Host "   - Or look for: 0.0.0.0/0 (allows all IPs)" -ForegroundColor Gray
Write-Host ""
Write-Host "3. If NO IPs are listed (or your IP is missing):" -ForegroundColor Yellow
Write-Host "   a) Click 'Add IP Address' button" -ForegroundColor White
Write-Host "   b) Click 'Allow Access from Anywhere' (0.0.0.0/0)" -ForegroundColor White
Write-Host "   c) Click 'Confirm'" -ForegroundColor White
Write-Host "   d) Wait 2-3 minutes for changes to take effect" -ForegroundColor White
Write-Host ""

Write-Host "Step 3: Verify Database User" -ForegroundColor Yellow
Write-Host ""
Write-Host "Check your database user credentials:" -ForegroundColor White
$dbUsersUrl = "https://cloud.mongodb.com/v2#/security/database/users"
Write-Host "1. Go to: $dbUsersUrl" -ForegroundColor Cyan
Write-Host "2. Verify user 'socraticalearn_db_user' exists" -ForegroundColor White
Write-Host "3. Check that the password matches: zF9JdKAA3zKF1DKp" -ForegroundColor White
Write-Host "4. Ensure user has 'readWrite' permissions on 'socratica_dev' database" -ForegroundColor White
Write-Host ""

Write-Host "Step 4: Test Connection After Fix" -ForegroundColor Yellow
Write-Host ""
Write-Host "After adding your IP to Network Access:" -ForegroundColor White
Write-Host "1. Wait 2-3 minutes" -ForegroundColor Gray
Write-Host "2. Try connecting in MongoDB Compass again" -ForegroundColor Gray
Write-Host "3. If successful, restart your Spring Boot application" -ForegroundColor Gray
Write-Host ""

Write-Host "=== Connection String Reference ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Your connection string:" -ForegroundColor White
Write-Host "mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev?retryWrites=true&w=majority" -ForegroundColor Gray
Write-Host ""

Write-Host "=== Common Issues ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "If you still get SSL errors after whitelisting:" -ForegroundColor Yellow
Write-Host "1. Check if you're behind a corporate firewall/proxy" -ForegroundColor White
Write-Host "2. Try from a different network (mobile hotspot)" -ForegroundColor White
Write-Host "3. Check MongoDB Atlas status: https://status.mongodb.com/" -ForegroundColor White
Write-Host "4. Verify cluster is running (not paused) in Atlas dashboard" -ForegroundColor White
Write-Host ""

Write-Host "=== Quick Links ===" -ForegroundColor Cyan
$networkUrl = "https://cloud.mongodb.com/v2#/security/network/whitelist"
$usersUrl = "https://cloud.mongodb.com/v2#/security/database/users"
$clustersUrl = "https://cloud.mongodb.com/v2#/clusters"
Write-Host "Network Access: $networkUrl" -ForegroundColor Cyan
Write-Host "Database Users: $usersUrl" -ForegroundColor Cyan
Write-Host "Clusters: $clustersUrl" -ForegroundColor Cyan
Write-Host ""
