# GitLab Authentication Setup Guide

This guide will help you resolve the two authentication warnings you're seeing in GitLab.

## Warning 1: SSH Key Required

**Message**: "You can't push or pull repositories using SSH until you add an SSH key to your profile."

### Solution: Add SSH Key

#### Step 1: Check if you have an existing SSH key
```bash
ls -la ~/.ssh
```

Look for files like `id_rsa.pub`, `id_ed25519.pub`, or `id_ecdsa.pub`.

#### Step 2: Generate a new SSH key (if you don't have one)
```bash
# Generate SSH key (use your GitLab email)
ssh-keygen -t ed25519 -C "your_email@example.com"

# Press Enter to accept default location (~/.ssh/id_ed25519)
# Enter a passphrase (optional but recommended)
```

#### Step 3: Copy your SSH public key
```bash
# macOS
cat ~/.ssh/id_ed25519.pub | pbcopy

# Linux
cat ~/.ssh/id_ed25519.pub | xclip -selection clipboard

# Or just display it
cat ~/.ssh/id_ed25519.pub
```

#### Step 4: Add SSH key to GitLab
1. Go to GitLab: https://gitlab.com
2. Click your **avatar** (top right) → **Preferences**
3. In the left sidebar, click **SSH Keys**
4. Paste your public key in the **Key** field
5. Give it a title (e.g., "My MacBook Pro")
6. Set expiration date (optional)
7. Click **Add key**

#### Step 5: Test SSH connection
```bash
ssh -T git@gitlab.com
```

You should see: "Welcome to GitLab, @username!"

#### Step 6: Update your Git remote to use SSH (if needed)
```bash
cd /Users/marateodorescu/socratica

# Check current remote
git remote -v

# If it shows HTTPS, change to SSH
git remote set-url origin git@gitlab.com:mara.b.teodorescu-group/socratica.git
```

---

## Warning 2: Password/Token Required for HTTPS

**Message**: "Your account is authenticated with SSO or SAML. To push and pull over HTTPS with Git using this account, you must set a password or set up a personal access token."

### Solution A: Use SSH (Recommended)

If you set up SSH keys (above), you can ignore this warning and use SSH for all Git operations.

### Solution B: Create Personal Access Token

If you prefer HTTPS, create a Personal Access Token:

#### Step 1: Create Personal Access Token
1. Go to GitLab: https://gitlab.com
2. Click your **avatar** (top right) → **Preferences**
3. In the left sidebar, click **Access Tokens**
4. Fill in the form:
   - **Token name**: "Socratica Development"
   - **Expiration date**: Set to 1 year from now
   - **Select scopes**: Check these boxes:
     - ✅ `api` - Full API access
     - ✅ `read_repository` - Read repository
     - ✅ `write_repository` - Write repository
5. Click **Create personal access token**
6. **IMPORTANT**: Copy the token immediately! You won't see it again.

#### Step 2: Use token for Git operations

When pushing/pulling, use the token as your password:

```bash
# First time pushing
git push origin main

# Username: your_gitlab_username
# Password: [paste your personal access token here]
```

#### Step 3: Store credentials (optional)
To avoid entering the token every time:

```bash
# Store credentials in macOS Keychain
git config --global credential.helper osxkeychain

# Or store in Git credential store (less secure)
git config --global credential.helper store
```

---

## Recommended Setup for Your Project

For the **Socratica** project, I recommend:

### Use SSH for Daily Development
✅ **Pros**: More secure, no password needed, easier workflow  
✅ **Best for**: Regular development work

**Setup**:
1. Generate and add SSH key (see above)
2. Update remote to SSH:
   ```bash
   git remote set-url origin git@gitlab.com:mara.b.teodorescu-group/socratica.git
   ```
3. Push commits:
   ```bash
   git push origin main
   ```

### Use Personal Access Token for CI/CD (if needed)
Personal access tokens are useful for:
- GitLab CI/CD pipelines
- Third-party integrations
- Automation scripts

---

## Quick Commands Summary

### Check Authentication Status
```bash
# Check SSH
ssh -T git@gitlab.com

# Check current remote
git remote -v

# Check Git config
git config --list
```

### Push Your Current Changes
After setting up SSH or token:

```bash
cd /Users/marateodorescu/socratica

# You have 3 commits ready to push
git push origin main
```

---

## Team Setup (Mara & Maria)

Both team members should:
1. ✅ Add SSH keys to their GitLab profiles
2. ✅ Clone the repository using SSH
3. ✅ Configure Git with their name and email:
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

---

## Troubleshooting

### "Permission denied (publickey)"
- Your SSH key is not added to GitLab
- Or you're using the wrong key
- Solution: Follow SSH setup steps above

### "Authentication failed"
- Token is incorrect or expired
- Solution: Create a new personal access token

### "Could not resolve host"
- Network/DNS issue
- Solution: Check internet connection

### "The project you were looking for could not be found"
- Wrong repository URL
- Or you don't have access
- Solution: Verify repository URL and access permissions

---

## What to Do Right Now

1. **Set up SSH key** (takes 2 minutes):
   ```bash
   ssh-keygen -t ed25519 -C "mara.b.teodorescu@example.com"
   cat ~/.ssh/id_ed25519.pub | pbcopy
   ```
   Then add to GitLab → Preferences → SSH Keys

2. **Update Git remote to SSH**:
   ```bash
   cd /Users/marateodorescu/socratica
   git remote set-url origin git@gitlab.com:mara.b.teodorescu-group/socratica.git
   ```

3. **Push your commits**:
   ```bash
   git push origin main
   ```

4. **Click "Don't show again"** on both warnings in GitLab!

---

## Additional Resources

- [GitLab SSH Keys Documentation](https://docs.gitlab.com/ee/user/ssh.html)
- [GitLab Personal Access Tokens](https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)
- [Git Credential Storage](https://git-scm.com/docs/git-credential-store)

---

**Created**: October 28, 2025  
**Last Updated**: October 28, 2025

