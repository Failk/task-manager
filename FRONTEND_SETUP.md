# Task Manager - Full Stack Setup Guide

This guide will help you set up and run the complete Task Manager application with both backend and frontend using Docker.

## Quick Start with Docker Compose

The easiest way to run the entire application is using Docker Compose. This will start all three services:
- PostgreSQL database
- Spring Boot backend
- React frontend

### Prerequisites

- Docker and Docker Compose installed
- At least 4GB of RAM available for Docker

### Steps

1. **Navigate to the project root directory:**

```bash
cd task-manager-backend
```

2. **Start all services:**

```bash
docker-compose up -d
```

This command will:
- Build the backend Spring Boot application
- Build the React frontend application
- Start PostgreSQL database
- Connect all services together

3. **Wait for services to start** (usually 30-60 seconds):

```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f
```

4. **Access the application:**

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **Database**: localhost:5432 (postgres/postgres)

### Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (database data)
docker-compose down -v
```

## Manual Setup (Without Docker)

### Backend Setup

1. **Start PostgreSQL:**

```bash
docker run -d \
  --name taskmanager-db \
  -e POSTGRES_DB=taskmanager \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine
```

2. **Run the backend:**

```bash
# From project root
mvn spring-boot:run
```

Backend will be available at http://localhost:8080

### Frontend Setup

1. **Install dependencies:**

```bash
cd frontend
npm install
```

2. **Configure environment:**

```bash
cp .env.example .env
# Edit .env if needed
```

3. **Start development server:**

```bash
npm run dev
```

Frontend will be available at http://localhost:3000

## Application Features

### User Registration & Login
1. Navigate to http://localhost:3000
2. Click "Sign up" to create a new account
3. Fill in your details (first name, last name, email, password)
4. Login with your credentials

### Task Management
- **Create Tasks**: Click "New Task" button
- **Task Types**: Choose between One-Time or Recurring tasks
- **Priorities**: A (Critical), B (Important), C (Nice to have), D (Delegate)
- **Context Tags**: @home, @work, @phone, @errands, @computer, @waiting, @anywhere
- **Reminders**: Set reminders with lead time and notification type

### Projects
- Organize tasks into projects
- Track project completion percentage
- Archive completed projects

### Calendar Views
- **Daily View**: See all tasks for a specific day
- **Weekly View**: 7-day overview with tasks
- **Monthly View**: Full month calendar

### Notifications
- Real-time WebSocket notifications
- Email notifications (requires SMTP configuration)
- Popup notifications in the app
- Snooze and acknowledge notifications

### Settings
- Update profile information
- Change password
- Configure notification preferences

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Docker Compose                      │
├─────────────────┬───────────────────┬──────────────────────┤
│   PostgreSQL    │   Spring Boot     │    React + Nginx     │
│   Database      │   Backend API     │    Frontend          │
│   Port: 5432    │   Port: 8080      │    Port: 3000        │
└─────────────────┴───────────────────┴──────────────────────┘
```

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL 15
- WebSocket (STOMP)
- Maven

**Frontend:**
- React 18
- Vite
- React Router
- Axios
- Tailwind CSS
- WebSocket (STOMP.js)
- Nginx (production)

## Configuration

### Backend Environment Variables

Edit `docker-compose.yml` to configure:

```yaml
environment:
  DB_USERNAME: postgres
  DB_PASSWORD: postgres
  JWT_SECRET: your-secret-key
  MAIL_HOST: smtp.gmail.com
  MAIL_USERNAME: your-email@gmail.com
  MAIL_PASSWORD: your-app-password
```

### Frontend Environment Variables

Edit `frontend/.env`:

```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws
```

## API Documentation

Once the backend is running, access the Swagger UI:

http://localhost:8080/api/v1/swagger-ui.html

This provides interactive API documentation with the ability to test endpoints.

## Design Patterns Implemented

1. **Factory Pattern**: TaskFactory for creating OneTimeTask and RecurringTask
2. **Strategy Pattern**: Calendar views (Daily, Weekly, Monthly)
3. **Observer Pattern**: Notification system (Email, Popup)
4. **Repository Pattern**: Data access layer with Spring Data JPA

## Common Issues & Solutions

### Port Already in Use

If you get a "port already in use" error:

```bash
# Check what's using the port
# Windows
netstat -ano | findstr :3000
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :3000
lsof -i :8080

# Change ports in docker-compose.yml or stop the conflicting service
```

### Database Connection Failed

```bash
# Check if PostgreSQL is running
docker-compose ps

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Frontend Can't Connect to Backend

1. Verify backend is running: http://localhost:8080/api/v1/actuator/health
2. Check CORS configuration in backend
3. Verify frontend environment variables

### WebSocket Connection Failed

1. Ensure backend WebSocket endpoint is accessible
2. Check JWT token is valid
3. Verify WS_URL in frontend configuration

## Development Workflow

### Making Changes to Backend

```bash
# Rebuild and restart backend
docker-compose up -d --build backend

# Or run locally
mvn spring-boot:run
```

### Making Changes to Frontend

```bash
# For development, run locally (hot reload)
cd frontend
npm run dev

# To rebuild Docker image
docker-compose up -d --build frontend
```

## Testing

### Backend Tests

```bash
mvn test
```

### Frontend Tests

```bash
cd frontend
npm test
```

## Production Deployment

### Building for Production

```bash
# Build all services
docker-compose build

# Tag images
docker tag taskmanager-backend:latest your-registry/taskmanager-backend:v1.0.0
docker tag taskmanager-frontend:latest your-registry/taskmanager-frontend:v1.0.0

# Push to registry
docker push your-registry/taskmanager-backend:v1.0.0
docker push your-registry/taskmanager-frontend:v1.0.0
```

### Production Considerations

1. **Use environment variables** for sensitive data
2. **Enable HTTPS** with SSL certificates
3. **Set up proper logging** and monitoring
4. **Configure database backups**
5. **Use a production-grade database** (managed PostgreSQL)
6. **Set strong JWT_SECRET**
7. **Configure SMTP** for email notifications
8. **Set up rate limiting** on API endpoints
9. **Enable CORS** only for your domain
10. **Use container orchestration** (Kubernetes, Docker Swarm)

## Monitoring

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Last 100 lines
docker-compose logs --tail=100 backend
```

### Health Checks

- Backend: http://localhost:8080/api/v1/actuator/health
- Frontend: http://localhost:3000 (should show login page)
- Database: `docker-compose exec postgres pg_isready`

## Backup and Restore

### Database Backup

```bash
# Backup
docker-compose exec postgres pg_dump -U postgres taskmanager > backup.sql

# Restore
docker-compose exec -T postgres psql -U postgres taskmanager < backup.sql
```

## Support

For issues, questions, or contributions:
- Check the documentation
- Review the code comments
- Open an issue on GitHub

## License

MIT License - Team Seaways

---

**Happy Task Managing! 🎯**
