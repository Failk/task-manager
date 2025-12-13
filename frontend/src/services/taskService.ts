import api from './api';
import {
  Task,
  TaskListItem,
  TaskInstance,
  CreateOneTimeTaskRequest,
  CreateRecurringTaskRequest,
  UpdateTaskRequest,
  TaskFilter,
  TaskComment,
  CreateCommentRequest,
} from '../types';

export const taskService = {
  getAll: async (filter?: TaskFilter): Promise<TaskListItem[]> => {
    const response = await api.get<TaskListItem[]>('/tasks', { params: filter });
    return response.data;
  },

  getById: async (id: number): Promise<Task> => {
    const response = await api.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  createOneTime: async (data: CreateOneTimeTaskRequest): Promise<Task> => {
    const response = await api.post<Task>('/tasks/one-time', data);
    return response.data;
  },

  createRecurring: async (data: CreateRecurringTaskRequest): Promise<Task> => {
    const response = await api.post<Task>('/tasks/recurring', data);
    return response.data;
  },

  update: async (id: number, data: UpdateTaskRequest): Promise<Task> => {
    const response = await api.put<Task>(`/tasks/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/tasks/${id}`);
  },

  complete: async (id: number): Promise<Task> => {
    const response = await api.post<Task>(`/tasks/${id}/complete`);
    return response.data;
  },

  defer: async (id: number, newDate: string): Promise<Task> => {
    const response = await api.post<Task>(`/tasks/${id}/defer`, null, {
      params: { newDate },
    });
    return response.data;
  },

  getOverdue: async (): Promise<TaskListItem[]> => {
    const response = await api.get<TaskListItem[]>('/tasks/overdue');
    return response.data;
  },

  getToday: async (): Promise<TaskListItem[]> => {
    const response = await api.get<TaskListItem[]>('/tasks/today');
    return response.data;
  },

  // Task instances
  getInstances: async (startDate: string, endDate: string): Promise<TaskInstance[]> => {
    const response = await api.get<TaskInstance[]>('/tasks/instances', {
      params: { startDate, endDate },
    });
    return response.data;
  },

  completeInstance: async (id: number): Promise<TaskInstance> => {
    const response = await api.post<TaskInstance>(`/tasks/instances/${id}/complete`);
    return response.data;
  },

  skipInstance: async (id: number): Promise<TaskInstance> => {
    const response = await api.post<TaskInstance>(`/tasks/instances/${id}/skip`);
    return response.data;
  },

  // Comments
  getComments: async (taskId: number): Promise<TaskComment[]> => {
    const response = await api.get<TaskComment[]>(`/tasks/${taskId}/comments`);
    return response.data;
  },

  addComment: async (taskId: number, data: CreateCommentRequest): Promise<TaskComment> => {
    const response = await api.post<TaskComment>(`/tasks/${taskId}/comments`, data);
    return response.data;
  },

  // Contexts
  addContext: async (taskId: number, contextId: number): Promise<Task> => {
    const response = await api.post<Task>(`/tasks/${taskId}/contexts/${contextId}`);
    return response.data;
  },

  removeContext: async (taskId: number, contextId: number): Promise<Task> => {
    const response = await api.delete<Task>(`/tasks/${taskId}/contexts/${contextId}`);
    return response.data;
  },

  reopen: async (id: number): Promise<Task> => {
    const response = await api.post<Task>(`/tasks/${id}/reopen`);
    return response.data;
  },
};
