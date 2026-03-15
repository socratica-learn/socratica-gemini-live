# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Accept build-time env vars so Vite can bake them into the JS bundle.
ARG VITE_API_BASE_URL=/api
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

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

# Default upstream keeps local docker-compose working.
ENV BACKEND_UPSTREAM=http://backend:8080

# Remove default nginx content
RUN rm -rf ./*

# Copy built assets from build stage
COPY --from=build /app/dist .

# Copy nginx configuration template. The official nginx entrypoint renders
# /etc/nginx/templates/*.template with envsubst on container startup.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
