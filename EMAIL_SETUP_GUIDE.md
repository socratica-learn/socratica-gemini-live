# Email Configuration Guide

## What is MAIL_USERNAME?

`MAIL_USERNAME` is the email address (or username) used to authenticate with the SMTP server. This is the account that will **send** password reset emails to your users.

## Can I Use a Socratica Email?

**Yes!** You can use any email address, including:
- A Socratica email (e.g., `noreply@socratica.com`, `support@socratica.com`)
- A Gmail account
- Any other email provider

## Email Provider SMTP Settings

### Option 1: Gmail

If using Gmail (even a Socratica-branded Gmail):

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # NOT your regular password!
MAIL_FROM=noreply@socratica.com  # Can be different from MAIL_USERNAME
```

**Important for Gmail:**
1. Enable 2-Factor Authentication
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the App Password (16 characters) in `MAIL_PASSWORD`

### Option 2: Custom Email Domain (Socratica Email)

If you have a custom email domain (e.g., `@socratica.com`), you need to find your email provider's SMTP settings:

#### Common Providers:

**Google Workspace (G Suite):**
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@socratica.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@socratica.com
```

**Microsoft 365 / Outlook:**
```env
MAIL_HOST=smtp.office365.com
MAIL_PORT=587
MAIL_USERNAME=noreply@socratica.com
MAIL_PASSWORD=your-password
MAIL_FROM=noreply@socratica.com
```

**SendGrid (Recommended for Production):**
```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=noreply@socratica.com
```

**Mailgun:**
```env
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=your-mailgun-username
MAIL_PASSWORD=your-mailgun-password
MAIL_FROM=noreply@socratica.com
```

**Zoho Mail:**
```env
MAIL_HOST=smtp.zoho.com
MAIL_PORT=587
MAIL_USERNAME=noreply@socratica.com
MAIL_PASSWORD=your-password
MAIL_FROM=noreply@socratica.com
```

## Example Configuration

If you have `noreply@socratica.com`:

```env
# For Google Workspace
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@socratica.com
MAIL_PASSWORD=your-app-password-here
MAIL_FROM=noreply@socratica.com
```

## Key Points

1. **MAIL_USERNAME** = The account that sends emails (SMTP authentication)
2. **MAIL_FROM** = The "From" address shown in emails (can be different)
3. **MAIL_PASSWORD** = Password or App Password for SMTP authentication
4. **MAIL_HOST** = SMTP server address (depends on your email provider)

## Testing

After configuring, test by:
1. Requesting a password reset
2. Checking if the email arrives
3. Checking backend logs for any SMTP errors

## Troubleshooting

### "Authentication failed"
- Check `MAIL_USERNAME` and `MAIL_PASSWORD` are correct
- For Gmail, make sure you're using an App Password, not your regular password
- Check if 2FA is enabled (required for Gmail App Passwords)

### "Connection refused"
- Check `MAIL_HOST` and `MAIL_PORT` are correct for your provider
- Check firewall/network settings

### "Email not received"
- Check spam folder
- Verify `MAIL_FROM` is a valid email address
- Check backend logs for sending errors
