import { format, parseISO, isToday, isTomorrow, isPast, differenceInDays } from 'date-fns';
import { Priority, TaskStatus } from '../types';

// Date utilities
export const formatDate = (dateString: string, formatStr = 'MMM d, yyyy'): string => {
  return format(parseISO(dateString), formatStr);
};

export const formatDateTime = (dateString: string): string => {
  return format(parseISO(dateString), 'MMM d, yyyy h:mm a');
};

export const formatRelativeDate = (dateString: string): string => {
  const date = parseISO(dateString);
  if (isToday(date)) return 'Today';
  if (isTomorrow(date)) return 'Tomorrow';
  const days = differenceInDays(date, new Date());
  if (days < 0) return `${Math.abs(days)} days ago`;
  if (days < 7) return format(date, 'EEEE');
  return format(date, 'MMM d');
};

export const isOverdue = (dateString: string, status: TaskStatus): boolean => {
  return isPast(parseISO(dateString)) && status !== TaskStatus.COMPLETED;
};

// Priority utilities
export const getPriorityColor = (priority: Priority): string => {
  const colors: Record<Priority, string> = {
    [Priority.A]: 'bg-red-500',
    [Priority.B]: 'bg-orange-500',
    [Priority.C]: 'bg-yellow-500',
    [Priority.D]: 'bg-green-500',
  };
  return colors[priority];
};

export const getPriorityBorderColor = (priority: Priority): string => {
  const colors: Record<Priority, string> = {
    [Priority.A]: 'border-red-500',
    [Priority.B]: 'border-orange-500',
    [Priority.C]: 'border-yellow-500',
    [Priority.D]: 'border-green-500',
  };
  return colors[priority];
};

export const getPriorityTextColor = (priority: Priority): string => {
  const colors: Record<Priority, string> = {
    [Priority.A]: 'text-red-500',
    [Priority.B]: 'text-orange-500',
    [Priority.C]: 'text-yellow-500',
    [Priority.D]: 'text-green-500',
  };
  return colors[priority];
};

export const getPriorityLabel = (priority: Priority): string => {
  const labels: Record<Priority, string> = {
    [Priority.A]: 'Critical',
    [Priority.B]: 'Important',
    [Priority.C]: 'Complete When Possible',
    [Priority.D]: 'Delegate/Defer',
  };
  return labels[priority];
};

// Status utilities
export const getStatusColor = (status: TaskStatus): string => {
  const colors: Record<TaskStatus, string> = {
    [TaskStatus.NOT_STARTED]: 'bg-gray-400',
    [TaskStatus.IN_PROGRESS]: 'bg-blue-500',
    [TaskStatus.COMPLETED]: 'bg-green-500',
    [TaskStatus.DEFERRED]: 'bg-purple-500',
  };
  return colors[status];
};

export const getStatusLabel = (status: TaskStatus): string => {
  const labels: Record<TaskStatus, string> = {
    [TaskStatus.NOT_STARTED]: 'Not Started',
    [TaskStatus.IN_PROGRESS]: 'In Progress',
    [TaskStatus.COMPLETED]: 'Completed',
    [TaskStatus.DEFERRED]: 'Deferred',
  };
  return labels[status];
};

// Local storage utilities
export const storage = {
  get: <T>(key: string, defaultValue: T): T => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : defaultValue;
    } catch {
      return defaultValue;
    }
  },
  set: <T>(key: string, value: T): void => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (e) {
      console.error('Error saving to localStorage', e);
    }
  },
  remove: (key: string): void => {
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.error('Error removing from localStorage', e);
    }
  },
};

// Class name utilities
export const cn = (...classes: (string | boolean | undefined)[]): string => {
  return classes.filter(Boolean).join(' ');
};
