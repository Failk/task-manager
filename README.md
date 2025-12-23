# Smart Personal Task Manager

A full-stack task management application based on the Franklin Covey methodology with Spring Boot backend and React frontend.

## 🚀 Quick Start

The easiest way to run the application is using Docker Compose:

### Windows
```bash
start.bat
```

### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

Or manually:
```bash
docker-compose up -d
```

Then access:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html

## 📚 Documentation

- [Frontend Setup Guide](frontend/README.md) - React application documentation
- [Full Stack Setup](FRONTEND_SETUP.md) - Complete setup and deployment guide
- [Backend API Documentation](http://localhost:8080/api/v1/swagger-ui.html) - Available when running

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.2**
- **Spring Security with JWT**
- **Spring Data JPA**
- **PostgreSQL 15**
- **WebSocket with STOMP**
- **Maven**
- **Docker**

### Frontend
- **React 18**
- **Vite**
- **Tailwind CSS**
- **React Router**
- **Axios**
- **WebSocket (STOMP.js)**
- **Nginx** (production)

## Design Patterns Implemented

1. **Factory Pattern** - `TaskFactory` for creating `OneTimeTask` and `RecurringTask` instances
2. **Strategy Pattern** - Calendar views (`DailyCalendarView`, `WeeklyCalendarView`, `MonthlyCalendarView`)
3. **Observer Pattern** - Notification system (`EmailNotificationObserver`, `PopupNotificationObserver`)
4. **Repository Pattern** - Data access layer using Spring Data JPA

## Project Structure

```
src/main/java/com/taskmanager/
├── config/           # Configuration classes
├── controller/       # REST API controllers
├── dto/              # Data Transfer Objects
├── entity/           # JPA entities
├── exception/        # Custom exceptions
├── factory/          # Factory pattern implementation
├── observer/         # Observer pattern implementation
├── repository/       # JPA repositories
├── security/         # JWT security components
├── service/          # Business logic services
└── strategy/         # Strategy pattern implementation
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login and get JWT token
- `POST /api/v1/auth/refresh` - Refresh JWT token

### Users
- `GET /api/v1/users/me` - Get current user profile
- `PUT /api/v1/users/me` - Update profile
- `PUT /api/v1/users/me/password` - Change password
- `GET/PUT /api/v1/users/me/notifications` - Notification preferences

### Projects
- `GET /api/v1/projects` - List all projects
- `POST /api/v1/projects` - Create project
- `GET /api/v1/projects/{id}` - Get project
- `PUT /api/v1/projects/{id}` - Update project
- `DELETE /api/v1/projects/{id}` - Delete project
- `POST /api/v1/projects/{id}/archive` - Archive project

### Tasks
- `GET /api/v1/tasks` - List all tasks
- `POST /api/v1/tasks/one-time` - Create one-time task
- `POST /api/v1/tasks/recurring` - Create recurring task
- `GET /api/v1/tasks/{id}` - Get task
- `PUT /api/v1/tasks/{id}` - Update task
- `DELETE /api/v1/tasks/{id}` - Delete task
- `POST /api/v1/tasks/{id}/complete` - Complete task
- `PUT /api/v1/tasks/{id}/status` - Update status
- `GET /api/v1/tasks/today` - Today's tasks
- `GET /api/v1/tasks/overdue` - Overdue tasks
- `POST /api/v1/tasks/filter` - Filter tasks

### Calendar (Strategy Pattern)
- `GET /api/v1/calendar/daily?date={date}` - Daily view
- `GET /api/v1/calendar/weekly?date={date}` - Weekly view
- `GET /api/v1/calendar/monthly?date={date}` - Monthly view

### Notifications (Observer Pattern)
- `GET /api/v1/notifications` - Get all notifications
- `GET /api/v1/notifications/unread` - Get unread notifications
- `POST /api/v1/notifications/{id}/acknowledge` - Acknowledge
- `POST /api/v1/notifications/{id}/snooze` - Snooze

### Contexts
- `GET /api/v1/contexts` - Get all contexts
- `POST /api/v1/contexts` - Create custom context

## 🎯 Features

- ✅ **Task Management**: Create, edit, complete, and delete tasks
- 🔄 **Recurring Tasks**: Daily, weekly, monthly, and yearly patterns
- 📊 **Priority System**: Franklin Covey A, B, C, D priorities
- 📁 **Project Organization**: Group tasks into projects
- 📅 **Calendar Views**: Daily, weekly, and monthly views
- 🏷️ **Context Tags**: GTD-style @home, @work, @phone, etc.
- 🔔 **Real-time Notifications**: WebSocket-based notifications
- 📧 **Email Notifications**: SMTP support for reminders
- 👤 **User Management**: Profile and preferences
- 🔐 **JWT Authentication**: Secure token-based auth
- 📱 **Responsive Design**: Mobile-friendly interface

## Running the Application

### Prerequisites
- Docker & Docker Compose (recommended)
- OR: Java 17+, Maven 3.8+, Node.js 18+, PostgreSQL 15

### Using Docker Compose (Recommended)

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
```bash
chmod +x start.sh
./start.sh
```

**Or manually:**
```bash
# Start all services (backend, frontend, database)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

This will start:
- PostgreSQL on port 5432
- Spring Boot backend on port 8080
- React frontend on port 3000

### Manual Setup (Without Docker)

**1. Start PostgreSQL:**
```bash
docker run -d \
  --name taskmanager-db \
  -e POSTGRES_DB=taskmanager \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

**2. Run the backend:**
```bash
mvn spring-boot:run
```

**3. Run the frontend:**
```bash
cd frontend
npm install
npm run dev
```

### Configuration

Environment variables:
- `DB_USERNAME` - Database username (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)
- `JWT_SECRET` - JWT signing key
- `MAIL_HOST` - SMTP host for email notifications
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password

## API Documentation

Swagger UI is available at: `http://localhost:8080/api/v1/swagger-ui.html`

OpenAPI spec at: `http://localhost:8080/api/v1/api-docs`

## WebSocket

Real-time notifications are delivered via WebSocket:
- Endpoint: `/api/v1/ws`
- User notifications: `/user/{userId}/queue/notifications`

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Franklin Covey Priority System

Tasks are prioritized using the Franklin Covey methodology:
- **A** - Critical: Must be done today
- **B** - Important: Should be done soon
- **C** - Nice to have: Can wait
- **D** - Delegate or Defer

## Default Context Tags

The system comes with pre-configured context tags:
- @home
- @work
- @phone
- @errands
- @computer
- @waiting
- @anywhere

## License

MIT License - Team Seaways
