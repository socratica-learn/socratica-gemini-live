# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Accept build-time env vars so Vite can bake them into the JS bundle.
# These must be ARGs (not ENV) so they are only available during the build.
ARG VITE_API_BASE_URL=/api
ARG VITE_GEMINI_API_KEY
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_GEMINI_API_KEY=$VITE_GEMINI_API_KEY

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build application
RUN npm run build

# Run stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx content
RUN rm -rf ./*

# Copy built assets from build stage
COPY --from=build /app/dist .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

