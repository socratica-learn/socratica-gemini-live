# Setup Secrets Guide - Step by Step

This guide will help you get all the secrets needed for your `.env` file.

## Required Secrets

1. **MongoDB Connection String** (`SPRING_DATA_MONGODB_URI`)
2. **JWT Secret** (`JWT_SECRET`)
3. **OpenAI API Key** (`OPENAI_API_KEY`)
4. **Google OAuth Credentials** (`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`)

---

## Step 1: Generate JWT Secret (Easiest - Do This First!)

The JWT secret is just a random string. You can generate it using PowerShell:

```powershell
# Generate a secure random string
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

Or use an online generator: https://www.random.org/strings/

**Example output:** `aB3xK9mP2qR7vT4wY8zN1cF6hJ0lM5sD9gH2jK`

Copy this value - you'll use it for `JWT_SECRET`.

---

## Step 2: Get OpenAI API Key

1. Go to: https://platform.openai.com/
2. Sign up or log in
3. Click on your profile (top right) → **"View API keys"**
4. Click **"Create new secret key"**
5. Give it a name (e.g., "Socratica Development")
6. **Copy the key immediately** (you won't see it again!)
7. Format: `sk-...` (starts with `sk-`)

This is your `OPENAI_API_KEY`.

---

## Step 3: Get MongoDB Connection String

You have two options:

### Option A: MongoDB Atlas (Cloud - Recommended)

1. Go to: https://www.mongodb.com/cloud/atlas
2. Sign up or log in
3. Create a new cluster (free tier is fine)
4. Wait for cluster to be created (~5 minutes)
5. Click **"Connect"** button
6. Choose **"Connect your application"**
7. Select **"Node.js"** and version **"5.5 or later"**
8. Copy the connection string (looks like: `mongodb+srv://username:password@cluster.mongodb.net/`)
9. Replace `<password>` with your database user password
10. Add your database name at the end: `...mongodb.net/socratica_dev`

**Example:** `mongodb+srv://myuser:mypassword@cluster0.xxxxx.mongodb.net/socratica_dev?retryWrites=true&w=majority`

This is your `SPRING_DATA_MONGODB_URI`.

### Option B: Local MongoDB

If you're running MongoDB locally:

```
mongodb://localhost:27017/socratica_dev
```

---

## Step 4: Get Google OAuth Credentials

1. Go to: https://console.cloud.google.com/
2. Sign in with your Google account
3. **Create a new project** (or select existing):
   - Click project dropdown (top left)
   - Click **"New Project"**
   - Name: "Socratica" (or your choice)
   - Click **"Create"**

4. **Enable Google+ API:**
   - Go to **"APIs & Services"** → **"Library"**
   - Search for "Google+ API" or "People API"
   - Click on it and click **"Enable"**

5. **Create OAuth Credentials:**
   - Go to **"APIs & Services"** → **"Credentials"**
   - Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
   - If prompted, configure OAuth consent screen:
     - User Type: **"External"** (unless you have Google Workspace)
     - App name: "Socratica"
     - User support email: Your email
     - Developer contact: Your email
     - Click **"Save and Continue"** through the steps
   - Application type: **"Web application"**
   - Name: "Socratica Web Client"
   - **Authorized redirect URIs:** Add:
     ```
     http://localhost:8080/api/auth/oauth/google/callback
     ```
   - Click **"Create"**

6. **Copy your credentials:**
   - You'll see a popup with **Client ID** and **Client Secret**
   - **Client ID** → `GOOGLE_CLIENT_ID`
   - **Client Secret** → `GOOGLE_CLIENT_SECRET`
   - (You can also find these later in Credentials page)

---

## Step 4b: Get Microsoft OAuth Credentials (Optional)

**Good news:** Microsoft OAuth is already fully implemented in your codebase! You just need to add the credentials.

1. Go to: https://portal.azure.com/
2. Sign in with your Microsoft account
3. **Create a new App Registration:**
   - Search for "Azure Active Directory" or "Microsoft Entra ID" in the search bar
   - Click on it, then go to **"App registrations"** (left sidebar)
   - Click **"+ New registration"**
   - Name: "Socratica" (or your choice)
   - Supported account types: **"Accounts in any organizational directory and personal Microsoft accounts"**
   - Redirect URI: Select **"Web"** and enter:
     ```
     http://localhost:8080/api/auth/oauth/microsoft/callback
     ```
   - Click **"Register"**

4. **Get your Client ID:**
   - On the app overview page, you'll see **"Application (client) ID"**
   - Copy this value → This is your `MICROSOFT_CLIENT_ID`

5. **Create a Client Secret:**
   - Go to **"Certificates & secrets"** (left sidebar)
   - Click **"+ New client secret"**
   - Description: "Socratica Development"
   - Expires: Choose "24 months" (or your preference)
   - Click **"Add"**
   - **Copy the secret value immediately** (you won't see it again!)
   - This is your `MICROSOFT_CLIENT_SECRET`

6. **Configure API Permissions (if needed):**
   - Go to **"API permissions"** (left sidebar)
   - Click **"+ Add a permission"**
   - Select **"Microsoft Graph"** → **"Delegated permissions"**
   - Add: `openid`, `email`, `profile`, `User.Read`
   - Click **"Add permissions"**

**Note:** Microsoft OAuth is already implemented in your code - you just need these credentials!

---

## Step 5: Fill in your `.env` file

Open `e:\TWENTE\Projects\socratica-1\.env` and fill in all the values:

```env
# MongoDB Configuration
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/socratica_dev
MONGODB_DATABASE=socratica_dev

# JWT Secret (the random string you generated)
JWT_SECRET=your-generated-secret-here

# OpenAI API Key
OPENAI_API_KEY=sk-your-openai-key-here

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth/google/callback

# Microsoft OAuth Configuration (Optional - already implemented!)
MICROSOFT_CLIENT_ID=your-microsoft-client-id-guid
MICROSOFT_CLIENT_SECRET=your-microsoft-client-secret
MICROSOFT_REDIRECT_URI=http://localhost:8080/api/auth/oauth/microsoft/callback

# Frontend URL
FRONTEND_URL=http://localhost:5173
```

---

## Quick Checklist

- [ ] Generated JWT secret
- [ ] Got OpenAI API key
- [ ] Set up MongoDB (Atlas or local)
- [ ] Created Google OAuth credentials
- [ ] (Optional) Created Microsoft OAuth credentials
- [ ] Filled in `.env` file with all values
- [ ] Tested with `docker compose up`

---

## Need Help?

If you get stuck on any step, let me know which one and I'll help you through it!
