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
- **Orchestration**: Docker Compose (dev), Kubernetes (production)

## 🏁 Getting Started


### Prerequisites
- Java 17 or higher
- Node.js 18+ and npm/yarn
- MongoDB 7+
- Docker

### Docker Setup – Recommended
```bash
docker-compose up -d
```

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
