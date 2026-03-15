# Development Setup

This guide will help you set up your local development environment for Socratica.

## Prerequisites

### Required Software

#### Backend Development
- **Java Development Kit (JDK) 17+**
  - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
  - Verify: `java -version`

- **Maven 3.9+**
  - Download: https://maven.apache.org/download.cgi
  - Verify: `mvn -version`

- **MongoDB 7+**
  - Verify: `mongod --version`

#### Frontend Development
- **Node.js 18+ (LTS)**
  - Download: https://nodejs.org/
  - Verify: `node --version`

- **npm 10+** (comes with Node.js)
  - Verify: `npm --version`

#### General Tools
- **Git 2.30+**
  - Download: https://git-scm.com/downloads
  - Verify: `git --version`

- **Docker 24+** (Optional, for containerized development)
  - Download: https://www.docker.com/products/docker-desktop
  - Verify: `docker --version`

### Recommended IDEs

- **IntelliJ IDEA** (Ultimate or Community) for backend
- **VS Code** with extensions for frontend:
  - Volar
  - ESLint
  - Prettier
  - Tailwind CSS IntelliSense

---

## Project Setup

### 1. Clone the Repository

```bash
git clone https://gitlab.com/mara.b.teodorescu-group/socratica.git
cd socratica
```

### 2. Backend Setup

#### Configure Application Properties

Create `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/socratica_dev
      auto-index-creation: true

logging:
  level:
    com.socratica: DEBUG
```

#### Set Environment Variables

Create `.env` file in `backend/` directory:
```bash
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/socratica_dev
```

**Note**: Never commit `.env` files to Git!

#### Install Dependencies and Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend should be running at `http://localhost:8080`

### 3. Frontend Setup

#### Install Dependencies

```bash
cd frontend
npm install
```

#### Configure Environment

Create `frontend/.env.development`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=Socratica Dev
```

#### Run Development Server

```bash
npm run dev
```

The frontend should be running at `http://localhost:5173`

---

## Docker Setup (Alternative)

If you prefer containerized development:

### 1. Build and Run with Docker Compose

```bash
docker-compose up -d
```

This will start:
- Backend on `http://localhost:8080`
- Frontend on `http://localhost:5173`
- MongoDB on `localhost:27017`

### 2. View Logs

```bash
docker-compose logs -f
```

### 3. Stop Services

```bash
docker-compose down
```

---

## Verify Installation

### Backend Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Frontend

Open browser: http://localhost:5173

You should see the Socratica homepage.

---

## Common Issues & Solutions

### Backend

**Issue**: Port 8080 already in use  
**Solution**: 
```bash
# Find and kill process using port 8080
lsof -ti:8080 | xargs kill -9
# Or change port in application.yml
server.port: 8081
```

**Issue**: Database connection refused  
**Solution**: 
- Ensure MongoDB is running
- Verify connection URI in `application-dev.yml`

**Issue**: Maven dependencies not downloading  
**Solution**:
```bash
mvn clean install -U
# Or clear Maven cache
rm -rf ~/.m2/repository
```

### Frontend

**Issue**: Node modules errors  
**Solution**:
```bash
rm -rf node_modules package-lock.json
npm install
```

**Issue**: Port 5173 already in use  
**Solution**:
```bash
# Kill process on port 5173
lsof -ti:5173 | xargs kill -9
```

**Issue**: CORS errors  
**Solution**: Ensure backend CORS configuration allows frontend origin (http://localhost:5173)

---

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/issue-123-description
```

### 2. Make Changes

- Write code following coding standards
- Add tests for new features
- Update documentation as needed

### 3. Run Tests

**Backend**:
```bash
cd backend
mvn test
```

**Frontend**:
```bash
cd frontend
npm run test
npm run lint
```

### 4. Commit Changes

```bash
git add .
git commit -m "feat(component): description"
```

### 5. Push and Create Merge Request

```bash
git push origin feature/issue-123-description
```

Then create a Merge Request in GitLab.

---

## Useful Commands

### Backend

```bash
# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate code coverage report
mvn jacoco:report

# Check code style
mvn checkstyle:check

# Package application
mvn clean package
```

### Frontend

```bash
# Run dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests
npm run test

# Run tests with coverage
npm run test:coverage

# Lint code
npm run lint

# Format code
npm run format
```

---

## IDE Configuration

### IntelliJ IDEA (Backend)

1. **Import Project**: File → Open → Select `backend/pom.xml`
2. **Set JDK**: File → Project Structure → Project SDK → Java 17
3. **Enable Annotation Processing**: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable
4. **Code Style**: Settings → Editor → Code Style → Import Scheme → IntelliJ IDEA code style XML
5. **Install Plugins**: Lombok, Spring Boot Assistant

### VS Code (Frontend)

1. **Open Folder**: File → Open Folder → Select `frontend/`
2. **Install Extensions**:
   - Volar
   - ESLint
   - Prettier
   - Tailwind CSS IntelliSense
   - GitLens
3. **Configure Settings**: Create `.vscode/settings.json`:

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "eslint.validate": [
    "javascript",
    "javascriptreact",
    "typescript",
    "typescriptreact",
    "vue"
  ]
}
```

---

## Additional Resources

- [[Tools and Technologies|tools-and-technologies]] - Full tech stack
- [[Contributing Guidelines|contributing-guidelines]] - How to contribute
- [[Coding Standards|coding-standards]] - Code style guide
- [[Testing Guidelines|testing-guidelines]] - Writing tests

---

## Getting Help

- Check the [GitLab Wiki](https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/home)
- Create an issue for technical problems
- Ask in team chat or contact Mara/Maria

---

**Last Updated**: October 28, 2025

