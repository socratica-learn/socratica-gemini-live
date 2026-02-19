# PowerShell script to help fix MongoDB Atlas connection issues

Write-Host ""
Write-Host "=== MongoDB Atlas Connection Fix ===" -ForegroundColor Cyan
Write-Host ""

# Get current public IP
Write-Host "Checking your current public IP address..." -ForegroundColor Yellow
try {
    $publicIP = (Invoke-WebRequest -Uri "https://api.ipify.org" -UseBasicParsing).Content.Trim()
    Write-Host "Your current public IP: $publicIP" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "Could not determine your public IP. Please check manually at https://www.whatismyip.com/" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "=== MongoDB Atlas Network Access Configuration ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Follow these steps to fix the connection:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Go to MongoDB Atlas Dashboard:" -ForegroundColor White
Write-Host "   https://cloud.mongodb.com/" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Navigate to Network Access:" -ForegroundColor White
Write-Host "   - Click on 'Network Access' in the left sidebar" -ForegroundColor Gray
Write-Host "   - Or go to: Security → Network Access" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Add your IP address:" -ForegroundColor White
if ($publicIP) {
    Write-Host "   - Click 'Add IP Address' button" -ForegroundColor Gray
    Write-Host "   - Enter your IP: $publicIP" -ForegroundColor Gray
    Write-Host "   - Click 'Confirm'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "   OR for testing (less secure):" -ForegroundColor Yellow
    Write-Host "   - Click 'Add IP Address'" -ForegroundColor Gray
    Write-Host "   - Click 'Allow Access from Anywhere' (0.0.0.0/0)" -ForegroundColor Gray
    Write-Host "   - Click 'Confirm'" -ForegroundColor Gray
} else {
    Write-Host "   - Click 'Add IP Address' button" -ForegroundColor Gray
    Write-Host "   - Click 'Allow Access from Anywhere' (0.0.0.0/0) for testing" -ForegroundColor Gray
    Write-Host "   - Click 'Confirm'" -ForegroundColor Gray
}
Write-Host ""
Write-Host "4. Wait 1-2 minutes for changes to propagate" -ForegroundColor White
Write-Host ""
Write-Host "5. Verify your connection string:" -ForegroundColor White
Write-Host "   Connection String (from application.yml):" -ForegroundColor Cyan
Write-Host "   mongodb+srv://socraticalearn_db_user:***@cluster0.3h3exfm.mongodb.net/socratica_dev" -ForegroundColor Gray
Write-Host ""
Write-Host "6. Restart your Spring Boot application" -ForegroundColor White
Write-Host ""

# Check if connection string has special characters in password
Write-Host "=== Password Encoding Check ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "If your password contains special characters, they need to be URL-encoded:" -ForegroundColor Yellow
Write-Host "  @ → %40" -ForegroundColor Gray
Write-Host "  # → %23" -ForegroundColor Gray
Write-Host "  $ → %24" -ForegroundColor Gray
Write-Host "  % → %25" -ForegroundColor Gray
Write-Host "  & → %26" -ForegroundColor Gray
Write-Host "  + → %2B" -ForegroundColor Gray
Write-Host "  = → %3D" -ForegroundColor Gray
Write-Host ""

# Test connection string format
Write-Host "=== Connection String Test ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "To test your connection string, you can:" -ForegroundColor Yellow
Write-Host "1. Use MongoDB Compass:" -ForegroundColor White
Write-Host "   - Download: https://www.mongodb.com/try/download/compass" -ForegroundColor Cyan
Write-Host "   - Paste your connection string and try to connect" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Use mongosh (if installed):" -ForegroundColor White
Write-Host "   mongosh 'mongodb+srv://USERNAME:PASSWORD@cluster0.3h3exfm.mongodb.net/socratica_dev'" -ForegroundColor Gray
Write-Host ""

Write-Host "=== Next Steps ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "After configuring Network Access:" -ForegroundColor Yellow
Write-Host "1. ✅ Add your IP to MongoDB Atlas Network Access" -ForegroundColor White
Write-Host "2. ⏳ Wait 1-2 minutes" -ForegroundColor White
Write-Host "3. 🔄 Restart your Spring Boot backend" -ForegroundColor White
Write-Host "4. ✅ Try signing up again" -ForegroundColor White
Write-Host ""
Write-Host "If the issue persists, check:" -ForegroundColor Yellow
Write-Host "- MongoDB Atlas status: https://status.mongodb.com/" -ForegroundColor Gray
Write-Host "- Your firewall/antivirus settings" -ForegroundColor Gray
Write-Host "- Corporate network proxy settings" -ForegroundColor Gray
Write-Host ""

Write-Host "=== Quick MongoDB Atlas Links ===" -ForegroundColor Cyan
Write-Host "Dashboard: https://cloud.mongodb.com/" -ForegroundColor Cyan
Write-Host "Network Access: https://cloud.mongodb.com/v2#/security/network/whitelist" -ForegroundColor Cyan
Write-Host "Database Access: https://cloud.mongodb.com/v2#/security/database/users" -ForegroundColor Cyan
Write-Host ""
