# Task Manager Frontend

A modern React frontend for the Smart Personal Task Manager application based on the Franklin Covey methodology.

## Features

- **Authentication**: JWT-based login and registration
- **Task Management**: Create, edit, delete, and complete tasks with priorities (A, B, C, D)
- **Recurring Tasks**: Support for daily, weekly, monthly, and yearly recurring tasks
- **Project Organization**: Group tasks into projects with progress tracking
- **Calendar Views**: Daily, weekly, and monthly calendar views
- **Context Tags**: GTD-style context tagging (@home, @work, @phone, etc.)
- **Real-time Notifications**: WebSocket-based notifications with email and popup support
- **Responsive Design**: Mobile-friendly interface using Tailwind CSS
- **User Settings**: Profile management and notification preferences

## Technology Stack

- **React 18**: Modern React with hooks
- **Vite**: Fast build tool and dev server
- **React Router**: Client-side routing
- **Axios**: HTTP client with interceptors
- **Tailwind CSS**: Utility-first CSS framework
- **React Icons**: Icon library
- **React Toastify**: Toast notifications
- **date-fns**: Date manipulation and formatting
- **STOMP.js**: WebSocket client for real-time notifications
- **Zustand**: State management (optional)

## Project Structure

```
frontend/
├── src/
│   ├── components/        # Reusable components
│   │   ├── layout/       # Layout components (Sidebar, Header)
│   │   ├── tasks/        # Task-related components
│   │   └── notifications/ # Notification components
│   ├── pages/            # Page components
│   │   ├── auth/         # Login and Register
│   │   ├── Dashboard.jsx
│   │   ├── Tasks.jsx
│   │   ├── Projects.jsx
│   │   ├── Calendar.jsx
│   │   └── Settings.jsx
│   ├── services/         # API service layer
│   │   ├── api.service.js
│   │   ├── auth.service.js
│   │   ├── task.service.js
│   │   ├── project.service.js
│   │   ├── calendar.service.js
│   │   ├── notification.service.js
│   │   ├── context.service.js
│   │   ├── user.service.js
│   │   └── websocket.service.js
│   ├── context/          # React contexts
│   │   └── AuthContext.jsx
│   ├── config/           # Configuration
│   │   └── api.js
│   ├── App.jsx           # Main app component
│   ├── main.jsx          # Entry point
│   └── index.css         # Global styles
├── public/               # Static assets
├── Dockerfile            # Docker configuration
├── nginx.conf            # Nginx configuration for production
├── vite.config.js        # Vite configuration
├── tailwind.config.js    # Tailwind CSS configuration
└── package.json          # Dependencies
```

## Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn
- Backend API running on `http://localhost:8080`

### Development Setup

1. Install dependencies:

```bash
npm install
```

2. Create environment file:

```bash
cp .env.example .env
```

3. Update `.env` with your backend URL:

```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws
```

4. Start development server:

```bash
npm run dev
```

The app will be available at `http://localhost:3000`

### Building for Production

```bash
npm run build
```

The built files will be in the `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## Docker Deployment

### Using Docker Compose (Recommended)

From the project root directory:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5432
- Backend API on port 8080
- Frontend on port 3000

### Building Docker Image Manually

```bash
cd frontend
docker build -t task-manager-frontend .
docker run -p 3000:80 task-manager-frontend
```

The application will be available at `http://localhost:3000`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API URL | `/api/v1` |
| `VITE_WS_URL` | WebSocket URL | `/ws` |

## API Integration

The frontend communicates with the backend using:

- **REST API**: For CRUD operations
- **WebSocket**: For real-time notifications using STOMP protocol
- **JWT Authentication**: Bearer token in Authorization header

### API Service Layer

All API calls are centralized in service files:

- `api.service.js`: Base Axios instance with interceptors
- `auth.service.js`: Authentication (login, register, logout)
- `task.service.js`: Task management
- `project.service.js`: Project management
- `calendar.service.js`: Calendar views
- `notification.service.js`: Notifications
- `user.service.js`: User profile and settings
- `context.service.js`: Context tags
- `websocket.service.js`: Real-time WebSocket connection

## Features in Detail

### Franklin Covey Priority System

Tasks are organized by priority:
- **Priority A**: Critical - Must be done today (Red)
- **Priority B**: Important - Should be done soon (Amber)
- **Priority C**: Nice to have - Can wait (Blue)
- **Priority D**: Delegate or Defer (Gray)

### Task Types

1. **One-Time Tasks**: Single occurrence tasks with due dates
2. **Recurring Tasks**: Tasks that repeat based on a pattern
   - Daily, Weekly, Bi-weekly, Monthly, Yearly
   - Configurable start/end dates and occurrences

### Calendar Views

- **Daily View**: Shows all tasks for a single day
- **Weekly View**: 7-day view with tasks organized by day
- **Monthly View**: Full month calendar with task indicators

### Contexts (GTD)

Default context tags available:
- @home, @work, @phone, @errands, @computer, @waiting, @anywhere

Users can create custom contexts.

### Real-time Notifications

- WebSocket connection for instant notifications
- Email and popup notification support
- Snooze functionality (15 minutes)
- Notification preferences management

## Styling

The application uses Tailwind CSS with a custom configuration:

- **Primary Color**: Blue (customizable in `tailwind.config.js`)
- **Priority Colors**: Red (A), Amber (B), Blue (C), Gray (D)
- **Responsive Design**: Mobile-first approach
- **Dark Mode**: Not implemented yet

### Custom CSS Classes

- `.btn-primary`, `.btn-secondary`, `.btn-danger`, `.btn-success`: Button styles
- `.input`: Form input styling
- `.card`: Card component styling
- `.badge-priority-{A|B|C|D}`: Priority badges
- `.badge-status-{status}`: Status badges

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance Optimization

- Code splitting with React.lazy
- Image optimization
- Gzip compression in production (Nginx)
- Asset caching
- API response caching where appropriate

## Security

- JWT token stored in localStorage
- Automatic token refresh
- Protected routes
- XSS protection
- CSRF protection via JWT

## Troubleshooting

### CORS Issues

If you encounter CORS errors:
1. Ensure backend allows `http://localhost:3000` in CORS configuration
2. Check that the API URL in `.env` is correct

### WebSocket Connection Failed

1. Verify backend WebSocket endpoint is accessible
2. Check that the WS_URL in `.env` is correct
3. Ensure JWT token is valid

### Build Errors

1. Clear node_modules and reinstall:
```bash
rm -rf node_modules package-lock.json
npm install
```

2. Clear Vite cache:
```bash
rm -rf node_modules/.vite
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License - Team Seaways

## Support

For issues and questions, please open an issue on the GitHub repository.
