# Tools and Technologies

This page documents all the tools, frameworks, and technologies used in the Socratica project.

## 🔗 External Resources

### Project Documentation
- **Google Drive**: [Socratica Project Files](https://drive.google.com/drive/folders/1Etu78NAkP8LjpqeQPAMbTtaDacx7qCi1?usp=sharing)
  - Requirements documents
  - Research materials
  - Meeting notes
  - Design assets

## 🎨 Design & Prototyping

### Figma
- **Purpose**: UI/UX Design, wireframes, high-fidelity mockups
- **Access**: [Figma Project Link] *(Add your Figma link here)*
- **Usage**: 
  - Design system components
  - User flow diagrams
  - Interactive prototypes
  - Design handoff to developers

### Stitch (Stitches.dev)
- **Purpose**: Prototyping and design system
- **Website**: https://stitches.dev/
- **Usage**:
  - Rapid prototyping
  - CSS-in-JS styling
  - Theme configuration
  - Component variants

### Other Design Tools
- **Lucidchart/Draw.io**: Architecture diagrams, flowcharts
- **Miro**: Brainstorming, user journey mapping

---

## 💻 Backend Technologies

### Core Framework
- **Java 17+**
  - Language: Java SE 17 (LTS)
  - Features: Records, Pattern Matching, Text Blocks
  - Website: https://www.oracle.com/java/

- **Spring Boot 3.x**
  - Framework for building production-ready applications
  - Version: 3.2+
  - Website: https://spring.io/projects/spring-boot
  - Key Modules:
    - Spring Web (REST APIs)
    - Spring Data JPA (Database access)
    - Spring Security (Authentication/Authorization)
    - Spring Validation (Input validation)

### Database
- **PostgreSQL 14+**
  - Primary database for structured data
  - Website: https://www.postgresql.org/
  - Features: ACID compliance, advanced indexing, JSON support

- **pgvector**
  - Vector similarity search extension for PostgreSQL
  - Website: https://github.com/pgvector/pgvector
  - Purpose: Semantic search for notes and study materials

### AI & Machine Learning
- **Gemini GenAI SDK**
  - Gemini 2.5 Flash for conversational AI and Socratic questioning
  - Gemini Live for speech-to-text
  - Gemini Live for text-to-speech

### Build Tools
- **Maven** or **Gradle**
  - Dependency management and build automation
  - Recommendation: Maven 3.9+
  - Website: https://maven.apache.org/

### Testing
- **JUnit 5**
  - Unit testing framework
  - Website: https://junit.org/junit5/

- **Mockito**
  - Mocking framework for tests
  - Website: https://site.mockito.org/

- **REST Assured**
  - API testing library
  - Website: https://rest-assured.io/

- **Testcontainers**
  - Integration testing with Docker containers
  - Website: https://www.testcontainers.org/

### Code Quality
- **Checkstyle**
  - Code style checking
  - Configuration: Google Java Style Guide

- **SpotBugs**
  - Static analysis for bug detection

- **JaCoCo**
  - Code coverage reporting

---

## 🎨 Frontend Technologies

### Core Framework
- **Vue 3**
  - Progressive JavaScript framework
  - Version: 3.4+
  - Website: https://vuejs.org/
  - API Style: Composition API with `<script setup>`

- **Vite**
  - Build tool and dev server
  - Website: https://vitejs.dev/
  - Features: Fast HMR, optimized builds

### State Management
- **Pinia**
  - Official state management for Vue 3
  - Website: https://pinia.vuejs.org/
  - Replaces Vuex with better TypeScript support

### UI Framework & Styling
- **TailwindCSS**
  - Utility-first CSS framework
  - Version: 3.x
  - Website: https://tailwindcss.com/
  - Features: Responsive design, dark mode, custom themes

- **Vuetify** or **PrimeVue** (Choose one)
  - **Vuetify 3**: Material Design component library
    - Website: https://vuetifyjs.com/
  - **PrimeVue**: Rich UI component library
    - Website: https://primevue.org/

### TypeScript
- **TypeScript 5+**
  - Typed superset of JavaScript
  - Website: https://www.typescriptlang.org/
  - Benefits: Type safety, better IDE support, fewer runtime errors

### Voice Integration
- **Web Speech API**
  - Browser-native speech recognition and synthesis
  - MDN: https://developer.mozilla.org/en-US/docs/Web/API/Web_Speech_API

### Testing
- **Vitest**
  - Unit testing framework (Vite-native)
  - Website: https://vitest.dev/

- **Vue Test Utils**
  - Component testing utilities
  - Website: https://test-utils.vuejs.org/

- **Playwright** or **Cypress**
  - E2E testing
  - Playwright: https://playwright.dev/
  - Cypress: https://www.cypress.io/

### Code Quality
- **ESLint**
  - JavaScript/TypeScript linting
  - Config: Vue 3 + TypeScript recommended rules

- **Prettier**
  - Code formatting
  - Website: https://prettier.io/

---

## 🔧 DevOps & Infrastructure

### Version Control
- **GitLab**
  - Repository: https://gitlab.com/mara.b.teodorescu-group/socratica
  - Features: Git, CI/CD, Issues, Wiki, Merge Requests

### CI/CD
- **GitLab CI/CD**
  - Configuration: `.gitlab-ci.yml`
  - Stages: Lint → Test → Build → Deploy
  - Runners: Docker-based

### Containerization
- **Docker**
  - Container platform
  - Version: 24+
  - Website: https://www.docker.com/

- **Docker Compose**
  - Multi-container orchestration for local development
  - Website: https://docs.docker.com/compose/

### Cloud Services (Future)
- **AWS** / **Azure** / **Google Cloud**
  - Cloud hosting (TBD)
  - Services: EC2/App Service, RDS/Database, S3/Blob Storage

### Monitoring & Logging
- **Prometheus**
  - Metrics collection
  - Website: https://prometheus.io/

- **Grafana**
  - Metrics visualization
  - Website: https://grafana.com/

- **ELK Stack** (Optional)
  - Elasticsearch, Logstash, Kibana for log management

---

## 🔐 Security Tools

- **Spring Security**
  - Authentication and authorization

- **OWASP Dependency Check**
  - Vulnerability scanning for dependencies

- **SonarQube** (Optional)
  - Code quality and security analysis
  - Website: https://www.sonarqube.org/

---

## 📚 Documentation Tools

### API Documentation
- **Swagger/OpenAPI**
  - REST API documentation
  - Interactive API explorer
  - Tool: Springdoc OpenAPI

### Code Documentation
- **JavaDoc**
  - Java code documentation

- **JSDoc**
  - JavaScript/TypeScript documentation

### Project Documentation
- **Markdown**
  - All documentation in markdown format
  - Stored in `/docs` directory and GitLab Wiki

---

## 🔄 Communication & Collaboration

### Project Management
- **GitLab Issues**
  - Task tracking, bug reports, features
  - Labels, milestones, time tracking

- **GitLab Boards**
  - Kanban-style workflow visualization

### File Sharing
- **Google Drive**
  - Shared folder: [Socratica Project Files](https://drive.google.com/drive/folders/1Etu78NAkP8LjpqeQPAMbTtaDacx7qCi1?usp=sharing)
  - Used for: Requirements, research, meeting notes

---

## 📦 Package Managers

### Backend
- **Maven Central**
  - Java dependency repository

### Frontend
- **npm** / **yarn** / **pnpm**
  - JavaScript package managers
  - Recommendation: npm 10+

---

## 🧪 Development Tools

### IDEs
- **IntelliJ IDEA**
  - Recommended for Java/Spring Boot
  - Website: https://www.jetbrains.com/idea/

- **VS Code**
  - Recommended for Vue/Frontend
  - Website: https://code.visualstudio.com/
  - Extensions: Volar, ESLint, Prettier, Tailwind CSS IntelliSense

### API Testing
- **Postman**
  - API testing and documentation
  - Website: https://www.postman.com/

- **Insomnia**
  - Alternative REST client
  - Website: https://insomnia.rest/

### Database Tools
- **DBeaver**
  - Universal database tool
  - Website: https://dbeaver.io/

- **pgAdmin**
  - PostgreSQL-specific tool
  - Website: https://www.pgadmin.org/

---

## 📖 Learning Resources

### Documentation
- [Spring Boot Guides](https://spring.io/guides)
- [Vue 3 Documentation](https://vuejs.org/guide/introduction.html)
- [TailwindCSS Docs](https://tailwindcss.com/docs)
- [PostgreSQL Tutorial](https://www.postgresqltutorial.com/)

### Video Tutorials
- [Spring Boot Tutorial - YouTube](https://www.youtube.com/results?search_query=spring+boot+tutorial)
- [Vue 3 Tutorial - Vue Mastery](https://www.vuemastery.com/)

---

## 🔄 Version Requirements Summary

| Tool/Framework | Minimum Version | Recommended Version |
|---------------|-----------------|---------------------|
| Java | 17 | 17 or 21 LTS |
| Spring Boot | 3.0 | 3.2+ |
| PostgreSQL | 14 | 15+ |
| Node.js | 18 | 20 LTS |
| Vue | 3.0 | 3.4+ |
| Docker | 20 | 24+ |
| Maven | 3.8 | 3.9+ |
| Git | 2.30 | Latest |

---

## 📝 Notes

- All team members should have access to the Google Drive folder
- Figma access should be requested from the design lead
- GitLab repository access is managed by project owners (Mara, Maria)
- API keys for OpenAI and other services will be managed securely via environment variables

---

**Last Updated**: October 28, 2025  
**Maintained By**: Mara & Maria

