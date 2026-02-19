# How to Enable 2-Factor Authentication for Gmail

This guide will help you enable 2-Factor Authentication (2FA) for your Gmail account, which is required to generate App Passwords for sending emails.

---

## Step 1: Go to Google Account Settings

1. Open your browser and go to: **https://myaccount.google.com/**
2. Sign in with your Gmail account

---

## Step 2: Navigate to Security Settings

1. In the left sidebar, click **"Security"**
2. Or go directly to: **https://myaccount.google.com/security**

---

## Step 3: Enable 2-Step Verification

1. Scroll down to the **"How you sign in to Google"** section
2. Look for **"2-Step Verification"**
3. Click on **"2-Step Verification"**

---

## Step 4: Start the Setup Process

1. Click **"Get started"** button
2. You'll be asked to confirm your password - enter it

---

## Step 5: Choose Your Phone Number

1. Enter your phone number (mobile phone recommended)
2. Choose how you want to receive codes:
   - **Text message** (SMS) - Recommended
   - **Phone call** - Voice call
3. Click **"Next"**

---

## Step 6: Verify Your Phone

1. Google will send you a verification code
2. Enter the code you receive
3. Click **"Next"**

---

## Step 7: Turn On 2-Step Verification

1. You'll see a confirmation screen
2. Click **"Turn On"** to enable 2-Step Verification

---

## Step 8: (Optional) Add Backup Options

Google will suggest adding backup options:
- **Backup phone number** - Another phone number
- **Backup codes** - Print or save codes for emergencies
- **Google Authenticator app** - For app-based codes

You can skip these for now or set them up later.

---

## ✅ Done!

2-Step Verification is now enabled. You can now generate App Passwords for your application.

---

## Next Step: Generate App Password

After enabling 2FA, you need to generate an App Password:

1. Go back to: **https://myaccount.google.com/security**
2. Scroll to **"2-Step Verification"**
3. Click on it, then scroll down to **"App passwords"**
4. Or go directly to: **https://myaccount.google.com/apppasswords**
5. Click **"Select app"** → Choose **"Mail"**
6. Click **"Select device"** → Choose **"Other (Custom name)"**
7. Enter a name like: **"Socratica Backend"**
8. Click **"Generate"**
9. **Copy the 16-character password** (it looks like: `abcd efgh ijkl mnop`)
10. Use this password in your `.env` file as `MAIL_PASSWORD`

**Important:** 
- The password will be shown only once - copy it immediately!
- Remove spaces when using it: `abcdefghijklmnop`
- This is different from your regular Gmail password

---

## Quick Links

- **Google Account Security**: https://myaccount.google.com/security
- **2-Step Verification**: https://myaccount.google.com/signinoptions/two-step-verification
- **App Passwords**: https://myaccount.google.com/apppasswords

---

## Troubleshooting

### "2-Step Verification" option not showing
- Make sure you're signed in to the correct Google account
- Some accounts (like Google Workspace) may have it managed by an administrator

### Can't receive verification code
- Check your phone number is correct
- Try the "Phone call" option instead of SMS
- Check your phone has signal/service

### "App passwords" option not showing
- Make sure 2-Step Verification is fully enabled
- Wait a few minutes after enabling 2FA
- Try refreshing the page

---

## Security Note

2-Step Verification adds an extra layer of security to your account. Even if someone gets your password, they can't access your account without your phone.
