# Azure OAuth Setup - Step by Step

This guide will walk you through setting up Microsoft OAuth in Azure Portal.

## Prerequisites
- A Microsoft account (personal or work/school)
- Access to Azure Portal

---

## Step 1: Access Azure Portal

1. Go to: **https://portal.azure.com/**
2. Sign in with your Microsoft account

---

## Step 2: Create App Registration

1. In the Azure Portal search bar (top), type: **"Microsoft Entra ID"** or **"Azure Active Directory"**
2. Click on **"Microsoft Entra ID"** from the results
3. In the left sidebar, click **"App registrations"**
4. Click **"+ New registration"** button (top left)

---

## Step 3: Configure App Registration

Fill in the form:

1. **Name:** `Socratica` (or any name you prefer)
2. **Supported account types:** 
   - Select: **"Accounts in any organizational directory and personal Microsoft accounts"**
   - This allows both personal Microsoft accounts and work/school accounts
3. **Redirect URI:**
   - Platform: Select **"Web"** from the dropdown
   - URI: Enter: `http://localhost:8080/api/auth/oauth/microsoft/callback`
4. Click **"Register"** button

---

## Step 4: Get Your Client ID

After registration, you'll be on the **Overview** page:

1. Find **"Application (client) ID"** - it's a GUID (looks like: `12345678-1234-1234-1234-123456789abc`)
2. **Copy this value** - this is your `MICROSOFT_CLIENT_ID`
3. Save it somewhere safe (you'll need it for your `.env` file)

---

## Step 5: Create Client Secret

1. In the left sidebar, click **"Certificates & secrets"**
2. Under **"Client secrets"** section, click **"+ New client secret"**
3. Fill in:
   - **Description:** `Socratica Development` (or any description)
   - **Expires:** Choose **"24 months"** (or your preference - longer is easier for development)
4. Click **"Add"**
5. **IMPORTANT:** Copy the **Value** immediately (you won't see it again!)
   - The value will look like: `abc123~DEF456ghi789...`
   - This is your `MICROSOFT_CLIENT_SECRET`
6. Save it somewhere safe

**⚠️ Warning:** If you don't copy the secret value now, you'll need to create a new secret!

---

## Step 6: Configure API Permissions (Required)

1. In the left sidebar, click **"API permissions"**
2. Click **"+ Add a permission"**
3. Select **"Microsoft Graph"**
4. Select **"Delegated permissions"**
5. Check the following permissions:
   - ✅ `openid` (usually already added)
   - ✅ `email`
   - ✅ `profile`
   - ✅ `User.Read`
6. Click **"Add permissions"** at the bottom
7. (Optional but recommended) Click **"Grant admin consent"** if you're an admin - this prevents users from seeing consent screens

---

## Step 7: Add Credentials to Your `.env` File

Open your `.env` file at: `e:\TWENTE\Projects\socratica-1\.env`

Add or update these lines:

```env
MICROSOFT_CLIENT_ID=your-client-id-guid-here
MICROSOFT_CLIENT_SECRET=your-client-secret-value-here
MICROSOFT_REDIRECT_URI=http://localhost:8080/api/auth/oauth/microsoft/callback
```

**Example:**
```env
MICROSOFT_CLIENT_ID=12345678-1234-1234-1234-123456789abc
MICROSOFT_CLIENT_SECRET=abc123~DEF456ghi789JKL012mno345PQR678
MICROSOFT_REDIRECT_URI=http://localhost:8080/api/auth/oauth/microsoft/callback
```

---

## Step 8: Test It!

1. Restart your Docker containers:
   ```powershell
   docker compose down
   docker compose up
   ```

2. Go to your login page: `http://localhost:5173/login`
3. Click the **"Microsoft"** button
4. You should be redirected to Microsoft login
5. After logging in, you should be redirected back to your app

---

## Troubleshooting

### "AADSTS50011: The redirect URI does not match"
- Make sure the redirect URI in Azure exactly matches: `http://localhost:8080/api/auth/oauth/microsoft/callback`
- Check for typos, extra spaces, or missing `http://`

### "Invalid client secret"
- The secret might have expired or been deleted
- Create a new client secret in Azure and update your `.env` file

### "Insufficient privileges"
- Make sure you've added the API permissions (Step 6)
- Try granting admin consent if you're an admin

### Can't find "Microsoft Entra ID"
- Try searching for "Azure Active Directory" instead
- Make sure you're signed in with an account that has access to Azure

---

## Quick Reference

**Where to find things in Azure:**
- **Client ID:** App registrations → Your app → Overview → Application (client) ID
- **Client Secret:** App registrations → Your app → Certificates & secrets → Client secrets
- **Redirect URI:** App registrations → Your app → Overview → Redirect URIs

---

## Need Help?

If you get stuck, let me know:
- What step you're on
- Any error messages you see
- Screenshots (if possible)

I can help you troubleshoot!
