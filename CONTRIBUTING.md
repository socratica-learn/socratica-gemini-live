# Contributing to Socratica

Thank you for your interest in contributing to Socratica! This document provides guidelines and instructions for contributing to the project.

## 🎯 Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Collaborate openly and transparently
- Prioritize project goals and user needs

## 🌲 Branch Naming Convention

Use descriptive branch names that follow these patterns:

- `feature/issue-{number}-{short-description}` - For new features
- `bugfix/issue-{number}-{short-description}` - For bug fixes
- `hotfix/issue-{number}-{short-description}` - For critical production fixes
- `docs/issue-{number}-{short-description}` - For documentation updates
- `refactor/issue-{number}-{short-description}` - For code refactoring

**Examples:**
- `feature/issue-5-user-authentication`
- `bugfix/issue-12-login-validation`
- `docs/issue-8-api-documentation`

## 📝 Commit Message Guidelines

Write clear, concise commit messages following this format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic changes)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```
feat(auth): add JWT token authentication

Implemented JWT-based authentication for API endpoints.
Added token generation and validation middleware.

Closes #15
```

```
fix(quiz): correct answer validation logic

Fixed bug where multiple choice answers were not properly validated.

Fixes #23
```

## 🔄 Development Workflow

### 1. Pick an Issue
- Browse open issues in GitLab
- Assign yourself to an issue
- Move the issue to "In Progress" on the board

### 2. Create a Branch
```bash
git checkout main
git pull origin main
git checkout -b feature/issue-5-description
```

### 3. Develop
- Write clean, well-documented code
- Follow coding standards (see below)
- Add tests for new features
- Update documentation as needed

### 4. Test Locally
```bash
# Backend
cd backend
./mvnw test
./mvnw spring-boot:run

# Frontend
cd frontend
npm run test
npm run lint
npm run dev
```

### 5. Commit Changes
```bash
git add .
git commit -m "feat(component): description"
```

### 6. Push and Create Merge Request
```bash
git push origin feature/issue-5-description
```

Then create a Merge Request in GitLab:
- Provide a clear description
- Reference the issue number
- Assign reviewers
- Add appropriate labels
- Set milestone if applicable

### 7. Code Review
- Address reviewer feedback
- Make requested changes
- Push updates to the same branch

### 8. Merge
- After approval and CI/CD success
- Squash commits if necessary
- Delete the branch after merge

## 💻 Coding Standards

### Java (Backend)

#### General Guidelines
- Follow Java naming conventions
- Use meaningful variable and method names
- Keep methods small and focused (< 20 lines preferred)
- Write JavaDoc for public APIs

#### Code Style
- Indentation: 4 spaces
- Line length: Maximum 120 characters
- Braces: K&R style
```java
public class Example {
    public void method() {
        if (condition) {
            // code
        }
    }
}
```

#### Spring Boot Best Practices
- Use constructor injection over field injection
- Separate concerns: Controller → Service → Repository
- Use DTOs for API requests/responses
- Handle exceptions with @ControllerAdvice

**Example:**
```java
@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {
    
    private final StudySessionService studySessionService;
    
    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }
    
    @PostMapping
    public ResponseEntity<StudySessionDto> createSession(@Valid @RequestBody CreateSessionRequest request) {
        StudySessionDto session = studySessionService.createSession(request);
        return ResponseEntity.ok(session);
    }
}
```

### Vue 3 (Frontend)

#### General Guidelines
- Use Composition API
- Keep components small and reusable
- Use TypeScript for type safety
- Follow Vue 3 style guide

#### Code Style
- Indentation: 2 spaces
- Component names: PascalCase
- Props and emits: camelCase
- Use `<script setup>` syntax

**Example:**
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  sessionId: string
  isActive: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  start: []
  stop: []
}>()

const isRunning = ref(false)

const statusText = computed(() => {
  return isRunning.value ? 'Running' : 'Stopped'
})

const handleStart = () => {
  isRunning.value = true
  emit('start')
}
</script>

<template>
  <div class="study-session">
    <h2>{{ statusText }}</h2>
    <button @click="handleStart">Start Session</button>
  </div>
</template>

<style scoped>
.study-session {
  padding: 1rem;
}
</style>
```

## 🧪 Testing Guidelines

### Backend Testing
- Write unit tests for services
- Write integration tests for controllers
- Use JUnit 5 and Mockito
- Aim for >80% code coverage

```java
@SpringBootTest
class StudySessionServiceTest {
    
    @Autowired
    private StudySessionService studySessionService;
    
    @MockBean
    private StudySessionRepository studySessionRepository;
    
    @Test
    void shouldCreateSession() {
        // Arrange
        CreateSessionRequest request = new CreateSessionRequest();
        
        // Act
        StudySessionDto result = studySessionService.createSession(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("New Session", result.getName());
    }
}
```

### Frontend Testing
- Write unit tests for composables and utilities
- Write component tests with Vue Test Utils
- Write E2E tests for critical flows
- Use Vitest for unit tests

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StudySession from '@/components/StudySession.vue'

describe('StudySession', () => {
  it('renders session name', () => {
    const wrapper = mount(StudySession, {
      props: { sessionId: '123', isActive: true }
    })
    expect(wrapper.text()).toContain('Study Session')
  })
})
```

## 📋 Issue Guidelines

### Creating Issues
- Use descriptive titles
- Provide clear descriptions
- Add steps to reproduce (for bugs)
- Add acceptance criteria (for features)
- Estimate time if possible
- Add appropriate labels

### Issue Labels
- `type::feature` - New feature
- `type::bug` - Bug fix
- `type::enhancement` - Improvement to existing feature
- `type::documentation` - Documentation updates
- `priority::high` - High priority
- `priority::medium` - Medium priority
- `priority::low` - Low priority
- `status::in-progress` - Currently being worked on
- `status::review` - Ready for review
- `component::backend` - Backend related
- `component::frontend` - Frontend related
- `component::infrastructure` - DevOps/Infrastructure

## ⏱ Time Tracking

- Estimate time when creating issues (use `/estimate` command)
- Log time spent (use `/spend` command)
- Be honest about time estimates and actual time spent

**GitLab Quick Actions:**
```
/estimate 2h 30m
/spend 1h 45m
/label ~"type::feature" ~"priority::high"
```

## 🔍 Code Review Checklist

### For Authors
- [ ] Code follows style guidelines
- [ ] Tests pass locally
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] No linter errors
- [ ] Self-review completed

### For Reviewers
- [ ] Code is clear and maintainable
- [ ] Logic is correct and efficient
- [ ] Tests are adequate
- [ ] Security considerations addressed
- [ ] Performance implications considered
- [ ] Documentation is clear

## 🚀 Release Process

1. Create a release branch from `develop`
2. Update version numbers
3. Update CHANGELOG.md
4. Create merge request to `main`
5. After merge, tag the release
6. Deploy to production
7. Merge back to `develop`

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Vue 3 Documentation](https://vuejs.org/)
- [GitLab CI/CD Documentation](https://docs.gitlab.com/ee/ci/)
- Project Wiki: [GitLab Wiki](https://gitlab.com/mara.b.teodorescu-group/socratica/-/wikis/home)

## ❓ Questions?

If you have questions about contributing, please:
1. Check the project Wiki
2. Ask in the GitLab issue comments
3. Contact the project leads

Thank you for contributing to Socratica! 🎓

