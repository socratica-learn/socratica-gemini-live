# GitLab Project Setup Guide

This guide provides step-by-step instructions for setting up your GitLab project with issues, labels, milestones, and boards.

## 📋 Table of Contents
1. [Labels Setup](#labels-setup)
2. [Milestones Setup](#milestones-setup)
3. [Initial Issues](#initial-issues)
4. [Project Board](#project-board)
5. [Wiki Setup](#wiki-setup)
6. [Git Workflow](#git-workflow)

---

## 🏷 Labels Setup

Go to: **Project → Settings → Labels** or **Issues → Labels**

### Type Labels
| Label Name | Color | Description |
|------------|-------|-------------|
| `type::feature` | `#428BCA` (Blue) | New feature or enhancement |
| `type::bug` | `#D9534F` (Red) | Bug fix |
| `type::task` | `#5BC0DE` (Light Blue) | General task |
| `type::documentation` | `#5CB85C` (Green) | Documentation improvements |
| `type::refactor` | `#F0AD4E` (Orange) | Code refactoring |
| `type::test` | `#9933CC` (Purple) | Testing related |

### Priority Labels
| Label Name | Color | Description |
|------------|-------|-------------|
| `priority::critical` | `#8B0000` (Dark Red) | Must be done immediately |
| `priority::high` | `#FF4444` (Red) | Important, do soon |
| `priority::medium` | `#FFA500` (Orange) | Moderate importance |
| `priority::low` | `#90EE90` (Light Green) | Nice to have |

### Status Labels
| Label Name | Color | Description |
|------------|-------|-------------|
| `status::blocked` | `#8B0000` (Dark Red) | Blocked by dependency |
| `status::in-progress` | `#428BCA` (Blue) | Currently being worked on |
| `status::review` | `#F0AD4E` (Orange) | Ready for code review |
| `status::ready` | `#5CB85C` (Green) | Ready to start |

### Component Labels
| Label Name | Color | Description |
|------------|-------|-------------|
| `component::backend` | `#6F4E7C` (Purple) | Backend/Java/Spring |
| `component::frontend` | `#1F77B4` (Blue) | Frontend/Vue |
| `component::database` | `#2CA02C` (Green) | Database related |
| `component::devops` | `#FF7F0E` (Orange) | CI/CD, deployment |
| `component::ai` | `#E377C2` (Pink) | AI/ML integration |
| `component::design` | `#BCBD22` (Yellow) | UI/UX design |

### Phase Labels
| Label Name | Color | Description |
|------------|-------|-------------|
| `phase::planning` | `#CCCCCC` (Gray) | Planning phase |
| `phase::mvp` | `#5BC0DE` (Light Blue) | MVP development |
| `phase::enhancement` | `#428BCA` (Blue) | Post-MVP enhancements |

---

## 🎯 Milestones Setup

Go to: **Project → Issues → Milestones → New milestone**

### Milestone 1: Project Foundation
- **Title**: `v0.1.0 - Foundation`
- **Start Date**: Today's date
- **Due Date**: +2 weeks
- **Description**:
  ```
  Initial project setup, requirements documentation, and architecture design.
  
  Goals:
  - Define functional and non-functional requirements
  - Create system architecture
  - Design UI/UX prototypes
  - Set up development environment
  - Configure CI/CD pipeline
  ```

### Milestone 2: Backend Core
- **Title**: `v0.2.0 - Backend Core`
- **Due Date**: +4 weeks from start
- **Description**:
  ```
  Core backend functionality implementation.
  
  Goals:
  - User authentication system
  - Database schema implementation
  - REST API endpoints
  - AI service integration
  ```

### Milestone 3: Frontend Core
- **Title**: `v0.3.0 - Frontend Core`
- **Due Date**: +6 weeks from start
- **Description**:
  ```
  Core frontend functionality implementation.
  
  Goals:
  - User interface components
  - Authentication flow
  - Study session interface
  - Voice input/output integration
  ```

### Milestone 4: MVP Release
- **Title**: `v1.0.0 - MVP`
- **Due Date**: +12 weeks from start
- **Description**:
  ```
  First production-ready release with core features.
  
  Goals:
  - Interactive study sessions
  - AI-powered questioning
  - Note upload and summarization
  - Quiz generation
  - Progress tracking
  ```

---

## 📝 Initial Issues to Create

### Phase 1: Planning & Design (Week 1-2)

#### Issue 1: Define Functional Requirements
```markdown
**Title**: Define Functional Requirements Document

**Labels**: ~"type::documentation" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara, Maria
**Estimate**: 4h
**Description**:
Create a comprehensive functional requirements document covering all features of Socratica.

**Tasks**:
- [ ] User management requirements
- [ ] Study session requirements
- [ ] AI interaction requirements
- [ ] Note management requirements
- [ ] Quiz system requirements
- [ ] Progress tracking requirements

**Deliverable**: `/docs/requirements/FUNCTIONAL_REQUIREMENTS.md` (already created!)
```

#### Issue 2: Define Non-Functional Requirements
```markdown
**Title**: Define Non-Functional Requirements Document

**Labels**: ~"type::documentation" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara, Maria
**Estimate**: 3h
**Description**:
Create a comprehensive non-functional requirements document covering performance, security, scalability, etc.

**Tasks**:
- [ ] Performance requirements
- [ ] Security requirements
- [ ] Scalability requirements
- [ ] Usability requirements
- [ ] Compliance requirements

**Deliverable**: `/docs/requirements/NON_FUNCTIONAL_REQUIREMENTS.md` (already created!)
```

#### Issue 3: Create System Architecture Design
```markdown
**Title**: Design System Architecture

**Labels**: ~"type::documentation" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara
**Estimate**: 6h
**Description**:
Design the overall system architecture including backend services, frontend structure, database schema, and AI integration.

**Tasks**:
- [ ] High-level architecture diagram
- [ ] Database schema design
- [ ] API endpoint design
- [ ] Component interaction diagrams
- [ ] Technology stack decision
- [ ] Security architecture

**Deliverable**: `/docs/architecture/` folder with diagrams and documentation
```

#### Issue 4: Create UI/UX Prototypes in Stitch/Figma
```markdown
**Title**: Design UI/UX Prototypes

**Labels**: ~"type::task" ~"component::design" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara, Maria
**Estimate**: 8h
**Description**:
Create high-fidelity UI/UX prototypes for all major screens and user flows using Figma and Stitch.

**Tasks**:
- [ ] User registration/login screens
- [ ] Dashboard design
- [ ] Study session interface
- [ ] Note upload and management interface
- [ ] Quiz interface
- [ ] Progress tracking dashboard
- [ ] Mobile responsive designs
- [ ] Design system (colors, typography, components)

**Deliverable**: 
- Figma project with all screens
- Stitch prototypes for key interactions
- Link added to GitLab Wiki
```

#### Issue 5: Set Up Development Environment
```markdown
**Title**: Set Up Development Environment

**Labels**: ~"type::task" ~"component::devops" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara, Maria
**Estimate**: 4h
**Description**:
Set up local development environment for both backend and frontend.

**Tasks**:
- [ ] Install Java 17, Maven, PostgreSQL
- [ ] Install Node.js, npm
- [ ] Configure PostgreSQL database
- [ ] Set up IDE (IntelliJ, VS Code)
- [ ] Configure environment variables
- [ ] Test backend runs locally
- [ ] Test frontend runs locally
- [ ] Document setup process in Wiki

**Deliverable**: Working local development environment
```

#### Issue 6: Configure CI/CD Pipeline
```markdown
**Title**: Configure GitLab CI/CD Pipeline

**Labels**: ~"type::task" ~"component::devops" ~"priority::high" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara
**Estimate**: 4h
**Description**:
Set up GitLab CI/CD pipeline for automated testing, building, and deployment.

**Tasks**:
- [ ] Create `.gitlab-ci.yml` (already created!)
- [ ] Configure GitLab Runner
- [ ] Set up backend lint and test stages
- [ ] Set up frontend lint and test stages
- [ ] Configure Docker image builds
- [ ] Test pipeline with sample commits
- [ ] Set up deployment stages (dev, prod)

**Deliverable**: Working CI/CD pipeline
```

#### Issue 7: Set Up Project Documentation
```markdown
**Title**: Set Up Project Documentation Structure

**Labels**: ~"type::documentation" ~"priority::medium" ~"phase::planning"
**Milestone**: v0.1.0 - Foundation
**Assignees**: Mara, Maria
**Estimate**: 2h
**Description**:
Create comprehensive project documentation structure and initial content.

**Tasks**:
- [x] Create README.md (done!)
- [x] Create CONTRIBUTING.md (done!)
- [ ] Set up GitLab Wiki pages
- [ ] Add tools and technologies page
- [ ] Add development setup guide
- [ ] Add coding standards documentation

**Deliverable**: Complete project documentation
```

### Phase 2: Backend Setup (Week 3-4)

#### Issue 8: Initialize Spring Boot Backend Project
```markdown
**Title**: Initialize Spring Boot Backend Project Structure

**Labels**: ~"type::task" ~"component::backend" ~"priority::high" ~"phase::mvp"
**Milestone**: v0.2.0 - Backend Core
**Assignees**: Mara
**Estimate**: 2h
**Description**:
Create initial Spring Boot project structure with necessary dependencies.

**Tasks**:
- [ ] Generate Spring Boot project (Spring Initializr)
- [ ] Add dependencies (Web, JPA, Security, PostgreSQL, etc.)
- [ ] Configure application.yml
- [ ] Set up package structure
- [ ] Create Docker configuration
- [ ] Add Checkstyle configuration

**Deliverable**: `/backend` folder with working Spring Boot skeleton
```

#### Issue 9: Implement Database Schema
```markdown
**Title**: Implement Database Schema and Entity Models

**Labels**: ~"type::feature" ~"component::backend" ~"component::database" ~"priority::high"
**Milestone**: v0.2.0 - Backend Core
**Assignees**: Mara
**Estimate**: 6h
**Description**:
Implement database schema and JPA entity models for all core tables.

**Tasks**:
- [ ] User entity and repository
- [ ] StudySession entity and repository
- [ ] Note entity and repository
- [ ] Quiz entity and repository
- [ ] Progress entity and repository
- [ ] Create Flyway migrations
- [ ] Add database indexes
- [ ] Write repository tests

**Deliverable**: Complete database schema with entity models
```

#### Issue 10: Implement User Authentication
```markdown
**Title**: Implement User Authentication and Authorization

**Labels**: ~"type::feature" ~"component::backend" ~"priority::high"
**Milestone**: v0.2.0 - Backend Core
**Assignees**: Mara
**Estimate**: 8h
**Description**:
Implement user registration, login, and JWT-based authentication.

**Tasks**:
- [ ] User registration endpoint
- [ ] Login endpoint (email/password)
- [ ] JWT token generation
- [ ] JWT token validation
- [ ] Password hashing (BCrypt)
- [ ] Spring Security configuration
- [ ] Role-based access control
- [ ] Write authentication tests

**Deliverable**: Working authentication system
```

#### Issue 11: Integrate OpenAI API
```markdown
**Title**: Integrate OpenAI API for AI Services

**Labels**: ~"type::feature" ~"component::backend" ~"component::ai" ~"priority::high"
**Milestone**: v0.2.0 - Backend Core
**Assignees**: Mara
**Estimate**: 6h
**Description**:
Integrate OpenAI API for GPT-4, Whisper (STT), and TTS services.

**Tasks**:
- [ ] Add OpenAI Java SDK dependency
- [ ] Create OpenAI service wrapper
- [ ] Implement chat completion (GPT-4)
- [ ] Implement speech-to-text (Whisper)
- [ ] Implement text-to-speech
- [ ] Add error handling and retry logic
- [ ] Write integration tests

**Deliverable**: OpenAI service ready for use
```

### Phase 3: Frontend Setup (Week 3-4)

#### Issue 12: Initialize Vue 3 Frontend Project
```markdown
**Title**: Initialize Vue 3 Frontend Project Structure

**Labels**: ~"type::task" ~"component::frontend" ~"priority::high" ~"phase::mvp"
**Milestone**: v0.3.0 - Frontend Core
**Assignees**: Maria
**Estimate**: 2h
**Description**:
Create initial Vue 3 project structure with Vite, TypeScript, and necessary libraries.

**Tasks**:
- [ ] Create Vue 3 project with Vite
- [ ] Add TypeScript configuration
- [ ] Install Pinia (state management)
- [ ] Install Vue Router
- [ ] Install TailwindCSS
- [ ] Install UI library (Vuetify/PrimeVue)
- [ ] Set up ESLint and Prettier
- [ ] Configure Vitest for testing

**Deliverable**: `/frontend` folder with working Vue 3 skeleton
```

#### Issue 13: Create Authentication UI
```markdown
**Title**: Create Authentication UI (Login/Register)

**Labels**: ~"type::feature" ~"component::frontend" ~"priority::high"
**Milestone**: v0.3.0 - Frontend Core
**Assignees**: Maria
**Estimate**: 6h
**Description**:
Implement login and registration user interface.

**Tasks**:
- [ ] Create LoginView component
- [ ] Create RegisterView component
- [ ] Form validation
- [ ] Connect to backend API
- [ ] Store JWT token
- [ ] Implement auth store (Pinia)
- [ ] Add loading states and error handling
- [ ] Write component tests

**Deliverable**: Working login and registration pages
```

#### Issue 14: Create Dashboard Layout
```markdown
**Title**: Create Main Dashboard Layout and Navigation

**Labels**: ~"type::feature" ~"component::frontend" ~"priority::high"
**Milestone**: v0.3.0 - Frontend Core
**Assignees**: Maria
**Estimate**: 5h
**Description**:
Create the main dashboard layout with navigation sidebar and header.

**Tasks**:
- [ ] Create DashboardLayout component
- [ ] Create NavigationSidebar component
- [ ] Create HeaderBar component
- [ ] Set up Vue Router routes
- [ ] Add protected route guards
- [ ] Responsive design (mobile/tablet/desktop)
- [ ] Write component tests

**Deliverable**: Main application layout with navigation
```

---

## 📊 Project Board Setup

Go to: **Project → Issues → Boards → New board**

### Create Board: "Socratica Development"

**Lists** (columns):
1. **Open** - All new issues
2. **To Do** - Prioritized and ready to start (~status::ready)
3. **In Progress** - Currently being worked on (~status::in-progress)
4. **Review** - Code review in progress (~status::review)
5. **Done** - Completed and closed

### Board Configuration:
- Enable **Scoped labels** for status::*
- Set **Milestone** filter for current sprint
- Add **Assignee** filter
- Enable **Weight** for time estimates

---

## 📚 Wiki Setup

Follow instructions in `/wiki/HOW_TO_UPLOAD_TO_GITLAB_WIKI.md`

### Pages to create:
1. `home` - Wiki homepage with navigation
2. `tools-and-technologies` - Tech stack documentation
3. `development-setup` - Setup guide
4. `architecture-overview` - System architecture (to be created)
5. `api-documentation` - API reference (to be created)
6. `contributing-guidelines` - Link to CONTRIBUTING.md

---

## 🔄 Git Workflow Setup

### Branch Protection Rules

Go to: **Settings → Repository → Protected branches**

**Protect `main` branch**:
- ✅ Allowed to merge: Maintainers only
- ✅ Allowed to push: No one
- ✅ Require approval from code owners: Yes
- ✅ Require passing pipeline: Yes

**Protect `develop` branch**:
- ✅ Allowed to merge: Developers
- ✅ Require approval: At least 1 approval
- ✅ Require passing pipeline: Yes

### Merge Request Settings

Go to: **Settings → Merge requests**

- ✅ Enable "Delete source branch" option by default
- ✅ Enable merge request approvals
- ✅ Pipelines must succeed
- ✅ All threads must be resolved

---

## ✅ First Commits to Make

After setting everything up, make these initial commits:

### Commit 1: Add Project Documentation
```bash
git checkout -b docs/initial-setup
git add README.md CONTRIBUTING.md .gitlab-ci.yml
git add .gitlab/issue_templates/
git add docs/
git commit -m "docs: add initial project documentation and CI/CD configuration"
git push origin docs/initial-setup
```

Create Merge Request → Get approval → Merge

### Commit 2: Add Backend Structure
```bash
git checkout -b feature/backend-structure
cd backend
# (Create Spring Boot project)
git add backend/
git commit -m "feat(backend): initialize Spring Boot project structure"
git push origin feature/backend-structure
```

Create Merge Request → Get approval → Merge

### Commit 3: Add Frontend Structure
```bash
git checkout -b feature/frontend-structure
cd frontend
# (Create Vue 3 project)
git add frontend/
git commit -m "feat(frontend): initialize Vue 3 project structure"
git push origin feature/frontend-structure
```

Create Merge Request → Get approval → Merge

---

## 🎉 Checklist

Use this checklist to track your setup progress:

### GitLab Configuration
- [ ] Create all labels
- [ ] Create all milestones
- [ ] Set up project board
- [ ] Configure branch protection
- [ ] Configure merge request settings

### Issues
- [ ] Create Issue #1: Functional Requirements
- [ ] Create Issue #2: Non-Functional Requirements
- [ ] Create Issue #3: System Architecture
- [ ] Create Issue #4: UI/UX Prototypes
- [ ] Create Issue #5: Development Environment
- [ ] Create Issue #6: CI/CD Pipeline
- [ ] Create Issue #7: Project Documentation
- [ ] Create Issue #8: Backend Project Structure
- [ ] Create Issue #9: Database Schema
- [ ] Create Issue #10: User Authentication
- [ ] Create Issue #11: OpenAI Integration
- [ ] Create Issue #12: Frontend Project Structure
- [ ] Create Issue #13: Authentication UI
- [ ] Create Issue #14: Dashboard Layout

### Wiki
- [ ] Upload home page
- [ ] Upload tools-and-technologies page
- [ ] Upload development-setup page
- [ ] Add Google Drive links

### First Commits
- [ ] Commit documentation
- [ ] Commit backend structure
- [ ] Commit frontend structure
- [ ] Test CI/CD pipeline

---

## 📞 Need Help?

Contact Mara or Maria for any questions or issues with the setup.

---

**Created**: October 28, 2025  
**Last Updated**: October 28, 2025

