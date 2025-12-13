# Smart Personal Task Manager

A full-stack task management application built with Spring Boot and React TypeScript, featuring the Franklin Covey priority system, recurring tasks, calendar views, and real-time notifications.

## Features

### Core Functionality
- **User Authentication**: JWT-based secure authentication with registration and login
- **Task Management**: Create, read, update, and delete tasks with rich metadata
- **Project Organization**: Group tasks into projects with color coding
- **Franklin Covey Priorities**: A/B/C/D priority system for effective task prioritization
- **Recurring Tasks**: Support for daily, weekly, and monthly recurring tasks
- **Calendar Views**: Daily, weekly, and monthly calendar views (Strategy pattern)
- **Context Tags**: GTD-style context tagging (@home, @work, @phone, etc.)
- **Reminders**: Email and popup reminder notifications (Observer pattern)
- **Real-time Updates**: WebSocket-based notifications for task changes

### Design Patterns
- **Strategy Pattern**: Calendar view rendering (Daily/Weekly/Monthly strategies)
- **Observer Pattern**: Notification/reminder system
- **Factory Pattern**: Task creation (one-time vs recurring)
- **Repository Pattern**: Data access layer

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL 15
- WebSocket (STOMP)
- Swagger/OpenAPI documentation

### Frontend
- React 18 with TypeScript
- Vite build tool
- Tailwind CSS v4
- React Router v6
- TanStack Query (React Query)
- Zustand state management
- Lucide React icons
- date-fns for date manipulation

## Project Structure

```
task-manager/
├── backend/
│   ├── src/main/java/com/taskmanager/
│   │   ├── config/         # Security, WebSocket, Swagger configs
│   │   ├── controller/     # REST API controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── entity/         # JPA entities
│   │   ├── factory/        # Task factory pattern
│   │   ├── observer/       # Notification observers
│   │   ├── repository/     # JPA repositories
│   │   ├── service/        # Business logic
│   │   └── strategy/       # Calendar view strategies
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── pages/          # Page components
│   │   ├── services/       # API services
│   │   ├── store/          # Zustand stores
│   │   ├── types/          # TypeScript types
│   │   └── utils/          # Utility functions
│   ├── Dockerfile
│   └── package.json
└── docker-compose.yml
```

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

### Running with Docker Compose

The easiest way to run the application:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

Services:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432

### Manual Setup

#### Database Setup

```sql
CREATE DATABASE taskmanager;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE taskmanager TO postgres;
```

#### Backend

```bash
cd backend

# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run
```

The backend will start on http://localhost:8080

#### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Development
npm run dev

# Production build
npm run build
```

The frontend will start on http://localhost:5173 (dev) or serve from dist/ (prod)

## API Documentation

Once the backend is running, access the Swagger UI at:
http://localhost:8080/swagger-ui.html

### Main API Endpoints

#### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get JWT token

#### Tasks
- `GET /api/v1/tasks` - Get all tasks (with filtering)
- `POST /api/v1/tasks/one-time` - Create one-time task
- `POST /api/v1/tasks/recurring` - Create recurring task
- `PUT /api/v1/tasks/{id}` - Update task
- `DELETE /api/v1/tasks/{id}` - Delete task
- `POST /api/v1/tasks/{id}/complete` - Mark task complete
- `GET /api/v1/tasks/today` - Get today's tasks
- `GET /api/v1/tasks/overdue` - Get overdue tasks

#### Projects
- `GET /api/v1/projects` - Get all projects
- `POST /api/v1/projects` - Create project
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project

#### Calendar
- `GET /api/v1/calendar/daily?date={date}` - Daily view
- `GET /api/v1/calendar/weekly?date={date}` - Weekly view
- `GET /api/v1/calendar/monthly?date={date}` - Monthly view

#### Contexts
- `GET /api/v1/contexts` - Get all contexts
- `POST /api/v1/contexts` - Create context

## Environment Variables

### Backend
| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/taskmanager` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (generated) | JWT signing secret |
| `JWT_EXPIRATION` | `86400000` | JWT expiration (ms) |
| `MAIL_USERNAME` | | SMTP username |
| `MAIL_PASSWORD` | | SMTP password |

### Frontend
| Variable | Default | Description |
|----------|---------|-------------|
| `VITE_API_URL` | `http://localhost:8080/api/v1` | Backend API URL |
| `VITE_WS_URL` | `http://localhost:8080/ws` | WebSocket URL |

## Default Data

The application seeds default context tags:
- @home - Tasks to do at home
- @work - Tasks to do at work  
- @phone - Tasks requiring phone calls
- @computer - Tasks requiring computer
- @errands - Tasks while running errands
- @anywhere - Tasks that can be done anywhere

## Franklin Covey Priority System

| Priority | Color | Description |
|----------|-------|-------------|
| A | Red | Critical/Vital - Must be done today |
| B | Orange | Important - Should be done soon |
| C | Yellow | Nice to have - Complete when possible |
| D | Green | Delegate/Defer - Can wait or be delegated |

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
