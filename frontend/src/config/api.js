export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';
export const WS_BASE_URL = import.meta.env.VITE_WS_URL || '/ws';

export const API_ENDPOINTS = {
  // Auth
  AUTH: {
    REGISTER: '/auth/register',
    LOGIN: '/auth/login',
    REFRESH: '/auth/refresh',
  },
  // Users
  USERS: {
    ME: '/users/me',
    UPDATE_PROFILE: '/users/me',
    CHANGE_PASSWORD: '/users/me/password',
    NOTIFICATIONS_PREFERENCES: '/users/me/notifications',
  },
  // Projects
  PROJECTS: {
    LIST: '/projects',
    CREATE: '/projects',
    GET: (id) => `/projects/${id}`,
    UPDATE: (id) => `/projects/${id}`,
    DELETE: (id) => `/projects/${id}`,
    ARCHIVE: (id) => `/projects/${id}/archive`,
    UNARCHIVE: (id) => `/projects/${id}/unarchive`,
    ARCHIVED: '/projects/archived',
  },
  // Tasks
  TASKS: {
    LIST: '/tasks',
    TODAY: '/tasks/today',
    OVERDUE: '/tasks/overdue',
    GET: (id) => `/tasks/${id}`,
    FILTER: '/tasks/filter',
    CREATE_ONE_TIME: '/tasks/one-time',
    CREATE_RECURRING: '/tasks/recurring',
    UPDATE: (id) => `/tasks/${id}`,
    DELETE: (id) => `/tasks/${id}`,
    COMPLETE: (id) => `/tasks/${id}/complete`,
    UPDATE_STATUS: (id) => `/tasks/${id}/status`,
    INSTANCE_STATUS: (instanceId) => `/tasks/instance/${instanceId}/status`,
    BY_PROJECT: (projectId) => `/tasks/project/${projectId}`,
    BY_CONTEXT: (contextId) => `/tasks/context/${contextId}`,
  },
  // Calendar
  CALENDAR: {
    DAILY: '/calendar/daily',
    WEEKLY: '/calendar/weekly',
    MONTHLY: '/calendar/monthly',
  },
  // Notifications
  NOTIFICATIONS: {
    LIST: '/notifications',
    UNREAD: '/notifications/unread',
    COUNT: '/notifications/count',
    ACKNOWLEDGE: (id) => `/notifications/${id}/acknowledge`,
    SNOOZE: (id) => `/notifications/${id}/snooze`,
    DELETE: (id) => `/notifications/${id}`,
  },
  // Contexts
  CONTEXTS: {
    LIST: '/contexts',
    DEFAULT: '/contexts/default',
    CREATE: '/contexts',
    DELETE: (id) => `/contexts/${id}`,
  },
};
