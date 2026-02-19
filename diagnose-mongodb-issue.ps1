# Comprehensive MongoDB Atlas Connection Diagnostic

Write-Host ""
Write-Host "=== MongoDB Atlas Connection Diagnostic ===" -ForegroundColor Cyan
Write-Host ""

# Step 1: Get public IP
Write-Host "[1/6] Checking your public IP..." -ForegroundColor Yellow
try {
    $publicIP = (Invoke-WebRequest -Uri "https://api.ipify.org" -UseBasicParsing).Content.Trim()
    Write-Host "   Your IP: $publicIP" -ForegroundColor Green
} catch {
    Write-Host "   Could not determine IP" -ForegroundColor Red
    $publicIP = $null
}
Write-Host ""

# Step 2: Test basic connectivity
Write-Host "[2/6] Testing connectivity to MongoDB Atlas..." -ForegroundColor Yellow
try {
    $testConnection = Test-NetConnection -ComputerName "cluster0.3h3exfm.mongodb.net" -Port 27017 -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
    if ($testConnection.TcpTestSucceeded) {
        Write-Host "   ✓ Port 27017 is reachable" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Port 27017 is NOT reachable (this is normal for SRV connections)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ⚠ Could not test port (this is normal)" -ForegroundColor Yellow
}
Write-Host ""

# Step 3: Check if mongosh is available
Write-Host "[3/6] Checking for MongoDB tools..." -ForegroundColor Yellow
$mongoshAvailable = $false
try {
    $mongoshVersion = mongosh --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✓ mongosh is installed" -ForegroundColor Green
        $mongoshAvailable = $true
    }
} catch {
    Write-Host "   ✗ mongosh is not installed (optional)" -ForegroundColor Yellow
}
Write-Host ""

# Step 4: Network Access Checklist
Write-Host "[4/6] Network Access Configuration Checklist" -ForegroundColor Yellow
Write-Host ""
Write-Host "   Please verify in MongoDB Atlas:" -ForegroundColor White
Write-Host "   [ ] Go to: https://cloud.mongodb.com/v2#/security/network/whitelist" -ForegroundColor Cyan
Write-Host "   [ ] Check if ANY IP addresses are listed" -ForegroundColor White
if ($publicIP) {
    Write-Host "   [ ] Look for your IP: $publicIP" -ForegroundColor White
}
Write-Host "   [ ] Look for: 0.0.0.0/0 (allows all IPs)" -ForegroundColor White
Write-Host ""
Write-Host "   If NO IPs are listed:" -ForegroundColor Red
Write-Host "   1. Click 'Add IP Address'" -ForegroundColor White
Write-Host "   2. Click 'Allow Access from Anywhere' (0.0.0.0/0)" -ForegroundColor White
Write-Host "   3. Click 'Confirm'" -ForegroundColor White
Write-Host "   4. Wait 3-5 minutes" -ForegroundColor White
Write-Host ""

# Step 5: Database User Check
Write-Host "[5/6] Database User Verification" -ForegroundColor Yellow
Write-Host ""
Write-Host "   Verify in MongoDB Atlas:" -ForegroundColor White
Write-Host "   [ ] Go to: https://cloud.mongodb.com/v2#/security/database/users" -ForegroundColor Cyan
Write-Host "   [ ] User 'socraticalearn_db_user' exists" -ForegroundColor White
Write-Host "   [ ] Password is correct: zF9JdKAA3zKF1DKp" -ForegroundColor White
Write-Host "   [ ] User has 'readWrite' permissions" -ForegroundColor White
Write-Host ""

# Step 6: Alternative Connection Methods
Write-Host "[6/6] Alternative Solutions" -ForegroundColor Yellow
Write-Host ""
Write-Host "   If Network Access is configured but still failing:" -ForegroundColor White
Write-Host ""
Write-Host "   Option A: Try standard connection (non-SRV)" -ForegroundColor Cyan
Write-Host "   Replace mongodb+srv:// with mongodb:// and use port 27017" -ForegroundColor Gray
Write-Host "   Note: You'll need to get the actual server IPs from Atlas" -ForegroundColor Gray
Write-Host ""
Write-Host "   Option B: Check cluster status" -ForegroundColor Cyan
Write-Host "   [ ] Go to: https://cloud.mongodb.com/v2#/clusters" -ForegroundColor Cyan
Write-Host "   [ ] Verify cluster is RUNNING (not paused)" -ForegroundColor White
Write-Host "   [ ] Check if cluster is in a different region" -ForegroundColor White
Write-Host ""
Write-Host "   Option C: Firewall/Proxy Issues" -ForegroundColor Cyan
Write-Host "   [ ] Try from a different network (mobile hotspot)" -ForegroundColor White
Write-Host "   [ ] Check Windows Firewall settings" -ForegroundColor White
Write-Host "   [ ] Check if behind corporate proxy" -ForegroundColor White
Write-Host "   [ ] Temporarily disable antivirus/firewall" -ForegroundColor White
Write-Host ""

# Connection String Info
Write-Host "=== Your Connection String ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev?retryWrites=true&w=majority" -ForegroundColor Gray
Write-Host ""

# Test with mongosh if available
if ($mongoshAvailable) {
    Write-Host "=== Testing Connection with mongosh ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Attempting to connect..." -ForegroundColor Yellow
    $connectionString = "mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev"
    try {
        $result = mongosh "$connectionString" --eval "db.adminCommand('ping')" --quiet 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Connection successful!" -ForegroundColor Green
        } else {
            Write-Host "✗ Connection failed" -ForegroundColor Red
            Write-Host $result -ForegroundColor Red
        }
    } catch {
        Write-Host "✗ Could not test connection" -ForegroundColor Red
    }
    Write-Host ""
}

Write-Host "=== Next Steps ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Verify Network Access is configured (most important!)" -ForegroundColor Yellow
Write-Host "2. Wait 3-5 minutes after making changes" -ForegroundColor Yellow
Write-Host "3. Try connecting again in MongoDB Compass" -ForegroundColor Yellow
Write-Host "4. If still failing, try from a different network" -ForegroundColor Yellow
$atlasStatusUrl = "https://status.mongodb.com/"
Write-Host "5. Check MongoDB Atlas status: $atlasStatusUrl" -ForegroundColor Yellow
Write-Host ""
