import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/authStore';
import { Layout } from './components/layout';
import { Login, Register } from './components/auth';
import { TaskForm } from './components/tasks';
import { Dashboard, Tasks, CalendarPage, Projects, TaskDetail, Settings } from './pages';
import { taskService } from './services/taskService';
import { webSocketService } from './services/websocketService';
import { CreateOneTimeTaskRequest, CreateRecurringTaskRequest } from './types';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Protected Route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, token } = useAuthStore();
  
  if (!isAuthenticated || !token) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
};

// Public Route component (redirects to dashboard if authenticated)
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, token } = useAuthStore();
  
  if (isAuthenticated && token) {
    return <Navigate to="/" replace />;
  }
  
  return <>{children}</>;
};

const App: React.FC = () => {
  const [showTaskForm, setShowTaskForm] = useState(false);
  const { token, isAuthenticated } = useAuthStore();

  // Connect WebSocket when authenticated
  useEffect(() => {
    if (isAuthenticated && token) {
      webSocketService.connect(token);
    }
    return () => {
      webSocketService.disconnect();
    };
  }, [isAuthenticated, token]);

  const handleCreateTask = async (data: CreateOneTimeTaskRequest | CreateRecurringTaskRequest) => {
    if ('frequency' in data) {
      await taskService.createRecurring(data);
    } else {
      await taskService.createOneTime(data);
    }
    // Trigger a refresh of the task list
    queryClient.invalidateQueries({ queryKey: ['tasks'] });
  };

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route
            path="/login"
            element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <Register />
              </PublicRoute>
            }
          />

          {/* Protected routes */}
          <Route
            element={
              <ProtectedRoute>
                <Layout onNewTask={() => setShowTaskForm(true)} />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Dashboard />} />
            <Route path="/tasks" element={<Tasks />} />
            <Route path="/tasks/:id" element={<TaskDetail />} />
            <Route path="/calendar" element={<CalendarPage />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/settings" element={<Settings />} />
          </Route>

          {/* Catch all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        {/* Global Task Form Modal */}
        <TaskForm
          isOpen={showTaskForm}
          onClose={() => setShowTaskForm(false)}
          onSubmit={handleCreateTask}
        />
      </BrowserRouter>
    </QueryClientProvider>
  );
};

export default App;
