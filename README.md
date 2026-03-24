# TaskFlow API

[![CI Pipeline](https://github.com/MuriloRip/taskflow-api/actions/workflows/ci.yml/badge.svg)](https://github.com/MuriloRip/taskflow-api/actions/workflows/ci.yml)

A REST API for task and project management built with **Java 17** and **Spring Boot 3.2**. Features JWT authentication, role-based access control, and comprehensive test coverage.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Security** with JWT authentication
- **Spring Data JPA** + **PostgreSQL**
- **Swagger/OpenAPI** for API documentation
- **JUnit 5** + **Mockito** for testing
- **Docker** + **Docker Compose**
- **GitHub Actions** for CI/CD

## Architecture

```
src/main/java/com/murilorip/taskflow/
├── config/          # Security and app configuration
├── controller/      # REST API endpoints
├── dto/             # Request/Response data transfer objects
├── entity/          # JPA entities and enums
├── exception/       # Custom exceptions and global handler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT token provider and auth filter
└── service/         # Business logic layer
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 16+ (or Docker)

### Run with Docker Compose

```bash
# Clone the repository
git clone https://github.com/MuriloRip/taskflow-api.git
cd taskflow-api

# Start all services
docker-compose up -d

# API will be available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui
```

### Run Locally

```bash
# Start PostgreSQL (via Docker or locally)
docker run -d --name taskflow-db \
  -e POSTGRES_DB=taskflow \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine

# Run the application
mvn spring-boot:run
```

### Run Tests

```bash
mvn clean test
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Projects

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | List user's projects (paginated) |
| GET | `/api/projects/{id}` | Get project details |
| POST | `/api/projects` | Create new project |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |

### Tasks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks/project/{projectId}` | List tasks by project |
| GET | `/api/tasks/my-tasks` | List user's assigned tasks |
| GET | `/api/tasks/{id}` | Get task details |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | API health status |

## Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "secret123"
  }'
```

### Create Project
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "My Project",
    "description": "Project description"
  }'
```

### Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Implement login page",
    "description": "Create the login form with validation",
    "status": "TODO",
    "priority": "HIGH",
    "projectId": 1,
    "dueDate": "2026-04-01"
  }'
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | postgres | Database username |
| `DB_PASSWORD` | postgres | Database password |
| `JWT_SECRET` | — | Secret key for JWT signing (min 256 bits) |
| `JWT_EXPIRATION` | 86400000 | Token expiration in ms (default: 24h) |
| `PORT` | 8080 | Server port |

## License

MIT License — see [LICENSE](LICENSE) for details.
