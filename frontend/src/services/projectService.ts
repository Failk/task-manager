import api from './api';
import { Project, CreateProjectRequest, UpdateProjectRequest } from '../types';

export const projectService = {
  getAll: async (includeArchived = false): Promise<Project[]> => {
    const response = await api.get<Project[]>('/projects', {
      params: { includeArchived },
    });
    return response.data;
  },

  getById: async (id: number): Promise<Project> => {
    const response = await api.get<Project>(`/projects/${id}`);
    return response.data;
  },

  create: async (data: CreateProjectRequest): Promise<Project> => {
    const response = await api.post<Project>('/projects', data);
    return response.data;
  },

  update: async (id: number, data: UpdateProjectRequest): Promise<Project> => {
    const response = await api.put<Project>(`/projects/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/projects/${id}`);
  },

  archive: async (id: number): Promise<Project> => {
    const response = await api.post<Project>(`/projects/${id}/archive`);
    return response.data;
  },

  unarchive: async (id: number): Promise<Project> => {
    const response = await api.post<Project>(`/projects/${id}/unarchive`);
    return response.data;
  },
};
