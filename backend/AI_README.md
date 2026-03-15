## AI API Overview

This document describes the AI endpoints implemented in the backend. All endpoints are **POST** requests that generate AI content via Gemini and return a simple response payload.

### Base URL

```
/api/ai
```

### Response Shape

```json
{
  "content": "AI-generated text output..."
}
```

### Authentication

Current config allows these endpoints without auth:

- `SecurityConfig` permits `/api/ai/**`

If you want authentication, remove it from the permit list and add auth handling.

---

## Endpoints

### 1) Written Evaluation

Create quizzes, exams, tests, or custom written evaluation content.

```
POST /api/ai/written-evaluation/quizzes
POST /api/ai/written-evaluation/exams
POST /api/ai/written-evaluation/tests
POST /api/ai/written-evaluation/custom
```

Request body:

```json
{
  "topic": "Linear Algebra",
  "questionCount": 10,
  "difficulty": "intermediate",
  "format": "multiple choice",
  "instructions": "Include 3 matrix questions"
}
```

### 2) Socratic Evaluation

Start a 1:1 session or generate an interruption question.

```
POST /api/ai/socratic-evaluation/session
POST /api/ai/socratic-evaluation/interruption
```

Session request:

```json
{
  "topic": "Ethics in AI",
  "studentLevel": "undergraduate",
  "goals": "Improve critical thinking",
  "conversationStyle": "curious and probing",
  "context": "Discuss fairness and bias"
}
```

Interruption request:

```json
{
  "sessionContext": "We are discussing algorithmic bias in hiring.",
  "lastUserMessage": "Bias happens because of data.",
  "interruptionGoal": "Push for deeper explanation"
}
```

### 3) Presentation Prep

```
POST /api/ai/presentation-prep/analyze
```

Request:

```json
{
  "transcript": "Hello everyone, today I will...",
  "slideNotes": "Slide 3 contains the results chart.",
  "audience": "technical hiring panel",
  "goals": "clear pacing and confidence"
}
```

### 4) Job Preparation

```
POST /api/ai/job-preparation/cover-letter
POST /api/ai/job-preparation/cv
POST /api/ai/job-preparation/interview
```

Cover letter:

```json
{
  "coverLetterText": "Dear Hiring Manager...",
  "jobDescription": "We are looking for a backend engineer...",
  "focusAreas": "clarity, impact"
}
```

CV:

```json
{
  "resumeText": "Experience: Java, Spring Boot...",
  "targetRole": "Backend Engineer",
  "focusAreas": "structure, impact"
}
```

Interview:

```json
{
  "role": "Backend Engineer",
  "jobDescription": "We value scalable systems...",
  "resumeText": "Experience: Java, Spring Boot...",
  "interviewType": "behavioral + technical",
  "difficulty": "intermediate"
}
```

### 5) Notes & Summaries

```
POST /api/ai/notes-summaries
```

Request:

```json
{
  "sourceText": "Long lecture transcript...",
  "desiredLength": "concise",
  "format": "bullet points",
  "focusAreas": "key definitions"
}
```

### 6) Teaching Advice

```
POST /api/ai/teaching-advice
```

Request:

```json
{
  "topic": "Newton's Laws",
  "audience": "high school students",
  "constraints": "no math-heavy explanations",
  "tone": "friendly and practical"
}
```

---

## Configuration

These endpoints use Gemini through `AiService`.

Required environment variables:

- `GEMINI_API_KEY` (backend only)
- `GEMINI_MODEL` (optional, default: `gemini-3-flash-preview`)
- `GEMINI_LIVE_MODEL` (optional, default: `gemini-live-2.5-flash-native-audio`)

Docker (backend service):

```
GEMINI_API_KEY: ${GEMINI_API_KEY}
```

---

## Implementation Notes

- Controller: `backend/src/main/java/com/socratica/controller/AiController.java`
- Service: `backend/src/main/java/com/socratica/service/AiService.java`
- DTOs: `backend/src/main/java/com/socratica/dto/*Request.java`

The service currently returns a plain text `content` field. If you want structured JSON outputs, update `AiResponse` and parse Gemini output into a structured schema.
