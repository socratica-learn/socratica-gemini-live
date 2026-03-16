# Socratica 🎓

**AI-Powered Interactive Study Assistant**

Socratica is an intelligent learning platform that helps students master their subjects through interactive study sessions, Socratic questioning, and comprehensive study tools. By combining AI-driven conversations with practical study aids, Socratica transforms passive learning into an active, engaging experience.

## 🌟 Vision

Traditional studying often lacks the interactive element of having someone to discuss concepts with. Socratica fills this gap by providing an AI tutor that:
- Listens to your presentations and explanations
- Asks clarifying questions to deepen understanding using the Socratic Method
- Generates quizzes and summaries from your notes
- Helps you prepare for exams and presentations

## 🚀 Key Features

### 1. Interactive Study Sessions
- **Voice-Based Presentations**: Present your subject matter verbally
- **AI Interruptions**: The AI asks targeted questions for clarification
- **Socratic Method**: Build stronger knowledge through explanation
- **Real-time Feedback**: Get instant insights on your understanding

### 2. Note Summarization
- Upload study materials (PDFs, documents, notes)
- Get concise, well-structured summaries
- Highlight key concepts and relationships

### 3. Quiz Generation
- Auto-generate quizzes from your notes
- Multiple choice, short answer, and essay questions
- Adaptive difficulty based on your performance

### 4. Study Planning
- Create study schedules
- Track progress and milestones
- Set goals and monitor achievements

### 5. Presentation Skills Training
- Practice explaining complex topics
- Get feedback on clarity and structure
- Build confidence for exams and presentations

## 🛠 Tech Stack

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **Database**: MongoDB
- **AI Integration**: Gemini GenAI SDK
- **Speech Services**: Gemini Live API
- **Build Tool**: Maven

### Frontend
- **Framework**: Vue 3 (Composition API)
- **Build Tool**: Vite
- **State Management**: Pinia
- **UI Library**: Vuetify / PrimeVue
- **Styling**: TailwindCSS
- **Voice Integration**: Web Speech API

### DevOps
- **CI/CD**: GitLab CI/CD
- **Containerization**: Docker
- **Build**: Cloud Build
- **Runtime**: Docker Compose (dev), Cloud Run (deployment target)
- **Infrastructure as Code**: Terraform

## 🏁 Getting Started


### Prerequisites
- Docker & Docker Compose

OR

- Java 17 or higher
- Node.js 18+ and npm/yarn
- MongoDB 7+


### Docker Setup – Recommended

For testing the project locally, it is recommended to use Docker Compose.

```bash
docker-compose up -d
```

Before starting Docker Compose, set `GEMINI_API_KEY` in `.env`. The backend now uses that same key for both standard Gemini calls and live voice websocket sessions.

Note: Keep in mind that social logins won't work with this setup. You can use the default email/password login or the live deployment.

### Backend Setup
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

## ☁️ Deploy To GCP

This repository includes Terraform and Cloud Build assets for deploying Socratica to:

- **Organization**: `809804464459`
- **Project**: `project-8d21f1f6-2009-4dcf-bff`
- **Region**: `europe-west4`

### Deployment Prerequisites

- `gcloud` CLI installed and authenticated
- `terraform` 1.5+
- Access to the target GCP project and org policies
- A populated `.env` file with the runtime secrets the backend needs

The deployment reuses the MongoDB connection string from `.env`, so make sure `SPRING_DATA_MONGODB_URI` points at a reachable non-local database before deploying.

### Deployment Steps

1. Authenticate the Google Cloud CLI and refresh Application Default Credentials:

```bash
gcloud auth login --update-adc
gcloud config set project project-8d21f1f6-2009-4dcf-bff
gcloud auth application-default set-quota-project project-8d21f1f6-2009-4dcf-bff
gcloud auth application-default login
```

If your org enforces periodic re-authentication and you see an `invalid_rapt` error, rerun the command above before retrying the deployment.

2. Review `.env` and confirm these values are set correctly:

- `SPRING_DATA_MONGODB_URI`
- `JWT_SECRET`
- `GEMINI_API_KEY`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

3. Run the deployment script from the repo root:

```bash
./scripts/gcp/deploy.sh
```

4. The script will:

- Bootstrap GCP services, IAM, service accounts, Secret Manager, and Artifact Registry with Terraform
- Deploy the backend with `GEMINI_API_KEY` injected from Secret Manager
- Build and push backend and frontend images with Cloud Build
- Deploy `socratica-backend` and `socratica-frontend` to Cloud Run
- Re-run Terraform to wire the final Cloud Run URLs into backend CORS and OAuth redirect settings

5. After the script completes, it prints the backend and frontend Cloud Run URLs.

For the full deployment notes, see [docs/gcp-cloud-run-deploy.md](docs/gcp-cloud-run-deploy.md).


## 📋 Development Workflow

1. **Create an issue** in GitLab for any new feature or bug
2. **Create a branch** from `main` using the naming convention: `feature/issue-number-description` or `bugfix/issue-number-description`
3. **Develop and test** your changes locally
4. **Commit** with clear, descriptive messages
5. **Push** to your branch and create a **Merge Request**
6. **Request review** from team members
7. **Merge** after approval and CI/CD pipeline success

## 🏷 Branch Strategy

- `main` - Production-ready code
- `develop` - Integration branch for features
- `frontend` - Frontend-specific development
- `backend` - Backend-specific development
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

## 👥 Team

- **Mara** - Project Lead & Full Stack Developer
- **Maria** - Co-Owner & Developer
- **Alex** - Full Stack Developer

## 📝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting merge requests.

## 📊 Project Management

- **Issues**: Track features, bugs, and tasks
- **Milestones**: Organize issues into release cycles
- **Labels**: Categorize issues (feature, bug, enhancement, documentation)
- **Board**: Kanban-style project board for visual workflow
- **Wiki**: Technical documentation and guides
- **Time Tracking**: Estimate and log time spent on issues


## 📞 Support

For questions or support, please create an issue in the GitLab project or contact the team leads.

---

**Built with ❤️ by the Socratica Team**
