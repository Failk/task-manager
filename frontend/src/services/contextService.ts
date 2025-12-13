import api from './api';
import { Context } from '../types';

export const contextService = {
  getAll: async (): Promise<Context[]> => {
    const response = await api.get<Context[]>('/contexts');
    return response.data;
  },

  getById: async (id: number): Promise<Context> => {
    const response = await api.get<Context>(`/contexts/${id}`);
    return response.data;
  },

  create: async (data: Partial<Context>): Promise<Context> => {
    const response = await api.post<Context>('/contexts', data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/contexts/${id}`);
  },
};
