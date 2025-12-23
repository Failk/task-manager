import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const contextService = {
  async getAllContexts() {
    const response = await api.get(API_ENDPOINTS.CONTEXTS.LIST);
    return response.data.data || [];
  },

  async getDefaultContexts() {
    const response = await api.get(API_ENDPOINTS.CONTEXTS.DEFAULT);
    return response.data.data || [];
  },

  async createContext(contextData) {
    const response = await api.post(API_ENDPOINTS.CONTEXTS.CREATE, contextData);
    return response.data.data;
  },

  async deleteContext(id) {
    await api.delete(API_ENDPOINTS.CONTEXTS.DELETE(id));
  },
};

export default contextService;
