# How to Upload Wiki Pages to GitLab

This document explains how to add these wiki pages to your GitLab project's Wiki.

## Method 1: Using GitLab Web Interface (Recommended)

### Step 1: Access the Wiki

1. Go to your GitLab project: https://gitlab.com/mara.b.teodorescu-group/socratica
2. In the left sidebar, click on **Wiki**
3. If this is your first time, click **Create your first page**

### Step 2: Create the Home Page

1. Click **New page** (or edit the existing home page)
2. Set **Title**: `home`
3. Copy the entire content from `wiki/home.md`
4. Paste into the content area
5. Set **Format**: `Markdown`
6. Click **Create page**

### Step 3: Create Additional Pages

For each wiki page (`tools-and-technologies.md`, `development-setup.md`, etc.):

1. Click **New page** in the Wiki
2. Set the **Title** to match the filename (without .md):
   - `tools-and-technologies`
   - `development-setup`
   - etc.
3. Copy the content from the corresponding file in `wiki/` folder
4. Paste into the content area
5. Set **Format**: `Markdown`
6. Click **Create page**

### Pages to Create:

1. ✅ `home` (from `home.md`)
2. ✅ `tools-and-technologies` (from `tools-and-technologies.md`)
3. ✅ `development-setup` (from `development-setup.md`)

---

## Method 2: Using Git (Advanced)

GitLab Wiki is actually a separate Git repository. You can clone it and push files directly.

### Step 1: Clone the Wiki Repository

```bash
git clone https://gitlab.com/mara.b.teodorescu-group/socratica.wiki.git
cd socratica.wiki
```

### Step 2: Copy Wiki Files

```bash
# Copy all wiki markdown files
cp /Users/marateodorescu/socratica/wiki/*.md .
```

### Step 3: Commit and Push

```bash
git add .
git commit -m "Add initial wiki pages: home, tools, development setup"
git push origin master
```

### Step 4: Verify

Visit https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/home to see your changes.

---

## Tips

### Wiki Naming Conventions
- Page names are lowercase with hyphens (e.g., `tools-and-technologies`)
- Links use format: `[[Link Text|page-name]]`
- The home page is special and should be named `home`

### Linking Between Pages
In your wiki content, links like this:
```markdown
[[Tools and Technologies|tools-and-technologies]]
```
Will create a clickable link to the `tools-and-technologies` page.

### Updating Pages
1. Navigate to the wiki page in GitLab
2. Click **Edit** button (top right)
3. Make your changes
4. Click **Save changes**

### Adding Images
1. Upload images to GitLab (Issues → Upload file, or use Wiki uploads)
2. Reference in markdown: `![Alt text](image-url)`

### Wiki Sidebar
GitLab automatically generates a sidebar with all wiki pages. You can customize it by creating a page called `_sidebar`.

---

## Quick Reference: GitLab Wiki URLs

Once created, your wiki pages will be accessible at:

- **Home**: https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/home
- **Tools and Technologies**: https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/tools-and-technologies
- **Development Setup**: https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/development-setup

---

## Need Help?

- [GitLab Wiki Documentation](https://docs.gitlab.com/ee/user/project/wiki/)
- Contact Mara or Maria for access issues

---

**Created**: October 28, 2025

