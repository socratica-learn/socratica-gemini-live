# Socratica Backend

Spring Boot backend application for the Socratica AI-powered study assistant.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **PostgreSQL 14+**
- **Maven**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **Gemini API Integration**

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.9+
- PostgreSQL 14+

### Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE socratica_dev;
CREATE USER socratica_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE socratica_dev TO socratica_user;
```

2. Create `.env` file in the `backend/` directory:
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/socratica_dev
DATABASE_USER=socratica_user
DATABASE_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_key
```

3. Build and run:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start at http://localhost:8080

### API Documentation

Once running, visit: http://localhost:8080/swagger-ui.html

### AI Endpoints

See `backend/AI_README.md` for all AI endpoints, request/response examples, and configuration details.

### Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Code Quality

```bash
# Run Checkstyle
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check
```

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/socratica/
│   │   │       ├── SocraticaApplication.java
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── dto/             # Data Transfer Objects
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── repository/      # Data repositories
│   │   │       ├── service/         # Business logic
│   │   │       ├── security/        # Security configuration
│   │   │       └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/        # Flyway migrations
│   └── test/
│       └── java/
│           └── com/socratica/
├── pom.xml
└── README.md
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DATABASE_URL` | PostgreSQL connection URL | Yes |
| `DATABASE_USER` | Database username | Yes |
| `DATABASE_PASSWORD` | Database password | Yes |
| `JWT_SECRET` | Secret key for JWT tokens | Yes |
| `JWT_EXPIRATION` | JWT token expiration (ms) | No (default: 3600000) |

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

## License

Proprietary - All rights reserved


