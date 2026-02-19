# Socratica Frontend

Vue 3 frontend application for the Socratica AI-powered study assistant.

## Tech Stack

- **Vue 3** (Composition API with `<script setup>`)
- **TypeScript**
- **Vite** (Build tool)
- **Pinia** (State management)
- **Vue Router** (Routing)
- **TailwindCSS** (Styling)
- **Axios** (HTTP client)
- **Vitest** (Testing)

## Getting Started

### Prerequisites

- Node.js 18+ (LTS)
- npm 10+

### Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env.development` file:
```bash
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=Socratica Dev
```

3. Run development server:
```bash
npm run dev
```

The frontend will start at http://localhost:5173

### Building for Production

```bash
npm run build
```

Build output will be in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## Testing

```bash
# Run tests in watch mode
npm run test

# Run tests once
npm run test:unit

# Run tests with coverage
npm run test:coverage
```

## Code Quality

```bash
# Lint code
npm run lint

# Format code
npm run format
```

## Project Structure

```
frontend/
├── public/              # Static assets
├── src/
│   ├── assets/          # Images, fonts, etc.
│   ├── components/      # Reusable Vue components
│   ├── views/           # Page components
│   ├── router/          # Vue Router configuration
│   ├── stores/          # Pinia stores
│   ├── services/        # API services
│   ├── composables/     # Vue composables
│   ├── utils/           # Utility functions
│   ├── types/           # TypeScript types
│   ├── App.vue          # Root component
│   └── main.ts          # Application entry point
├── index.html
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
└── package.json
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `VITE_API_BASE_URL` | Backend API base URL | Yes |
| `VITE_APP_TITLE` | Application title | No |

## Component Guidelines

- Use Composition API with `<script setup>`
- Use TypeScript for type safety
- Keep components small and focused
- Use Pinia stores for shared state
- Follow Vue 3 style guide

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

## License

Proprietary - All rights reserved

