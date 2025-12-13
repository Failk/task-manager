// Enums
export enum Priority {
  A = 'A',
  B = 'B',
  C = 'C',
  D = 'D'
}

export enum TaskStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  DEFERRED = 'DEFERRED'
}

export enum Frequency {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  CUSTOM = 'CUSTOM'
}

export enum EndCondition {
  NEVER = 'NEVER',
  AFTER_OCCURRENCES = 'AFTER_OCCURRENCES',
  BY_DATE = 'BY_DATE'
}

export enum ReminderType {
  EMAIL = 'EMAIL',
  POPUP = 'POPUP'
}

// User types
export interface User {
  id: number;
  email: string;
  username: string;
  fullName: string;
  createdAt: string;
  lastLogin?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
  fullName: string;
}

// Project types
export interface Project {
  id: number;
  name: string;
  description?: string;
  color: string;
  archived: boolean;
  createdAt: string;
  completionPercentage: number;
  taskCount: number;
  completedTaskCount: number;
}

export interface CreateProjectRequest {
  name: string;
  description?: string;
  color?: string;
}

export interface UpdateProjectRequest {
  name?: string;
  description?: string;
  color?: string;
}

// Task types
export interface Context {
  id: number;
  name: string;
  description?: string;
  color?: string;
  icon?: string;
}

export interface Reminder {
  id: number;
  type: ReminderType;
  leadTimeMinutes: number;
  reminderTime: string;
  sent: boolean;
  acknowledged: boolean;
}

export interface RecurrencePattern {
  frequency: Frequency;
  interval: number;
  daysOfWeek?: number[];
  dayOfMonth?: number;
  endCondition: EndCondition;
  occurrenceCount?: number;
  endDate?: string;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  dueDate: string;
  priority: Priority;
  status: TaskStatus;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  estimatedDurationMinutes?: number;
  projectId?: number;
  projectName?: string;
  project?: Project;
  recurringTaskId?: number;
  contexts: Context[];
  reminders: Reminder[];
  taskType: 'ONE_TIME' | 'RECURRING';
  recurrencePattern?: RecurrencePattern;
}

export interface TaskListItem {
  id: number;
  title: string;
  dueDate: string;
  priority: Priority;
  status: TaskStatus;
  projectName?: string;
  contextNames: string[];
  taskType: 'ONE_TIME' | 'RECURRING';
}

export interface TaskInstance {
  id: number;
  recurringTaskId: number;
  taskTitle: string;
  scheduledDate: string;
  status: TaskStatus;
  completedAt?: string;
  skipped: boolean;
  overrideTitle?: string;
  overrideDescription?: string;
}

export interface CreateOneTimeTaskRequest {
  title: string;
  description?: string;
  dueDate: string;
  priority: Priority;
  estimatedDurationMinutes?: number;
  projectId?: number;
  contextTags?: string[];
  reminders?: CreateReminderRequest[];
}

export interface CreateRecurringTaskRequest {
  title: string;
  description?: string;
  startDate: string;
  priority: Priority;
  estimatedDurationMinutes?: number;
  projectId?: number;
  contextTags?: string[];
  reminders?: CreateReminderRequest[];
  frequency: Frequency;
  interval?: number;
  daysOfWeek?: number[];
  dayOfMonth?: number;
  endCondition: EndCondition;
  occurrenceCount?: number;
  endDate?: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  dueDate?: string;
  priority?: Priority;
  status?: TaskStatus;
  estimatedDurationMinutes?: number;
  projectId?: number;
  contextTags?: string[];
}

export interface CreateReminderRequest {
  type: ReminderType;
  leadTimeMinutes: number;
}

export interface TaskFilter {
  projectId?: number;
  priority?: Priority;
  status?: TaskStatus;
  startDate?: string;
  endDate?: string;
  contextName?: string;
  search?: string;
}

// Calendar types
export interface CalendarView {
  viewType: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  startDate: string;
  endDate: string;
  tasks: TaskListItem[];
  metadata: CalendarMetadata;
}

export interface CalendarMetadata {
  title: string;
  totalTasks: number;
  completedTasks: number;
  tasksByPriority: Record<Priority, number>;
}

// Comment types
export interface TaskComment {
  id: number;
  content: string;
  authorId: number;
  authorName: string;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}

// Notification types
export interface Notification {
  id: string;
  type: 'REMINDER' | 'TASK_DUE' | 'TASK_OVERDUE' | 'PROJECT_UPDATE' | 'INFO';
  title: string;
  message: string;
  taskId?: number;
  createdAt: string;
  read: boolean;
}

// API Response types
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  errors?: Record<string, string>;
}
