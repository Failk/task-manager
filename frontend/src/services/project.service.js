import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const projectService = {
  async getAllProjects() {
    const response = await api.get(API_ENDPOINTS.PROJECTS.LIST);
    return response.data.data || [];
  },

  async getArchivedProjects() {
    const response = await api.get(API_ENDPOINTS.PROJECTS.ARCHIVED);
    return response.data.data || [];
  },

  async getProject(id) {
    const response = await api.get(API_ENDPOINTS.PROJECTS.GET(id));
    return response.data.data;
  },

  async createProject(projectData) {
    const response = await api.post(API_ENDPOINTS.PROJECTS.CREATE, projectData);
    return response.data.data;
  },

  async updateProject(id, projectData) {
    const response = await api.put(API_ENDPOINTS.PROJECTS.UPDATE(id), projectData);
    return response.data.data;
  },

  async deleteProject(id) {
    await api.delete(API_ENDPOINTS.PROJECTS.DELETE(id));
  },

  async archiveProject(id) {
    const response = await api.post(API_ENDPOINTS.PROJECTS.ARCHIVE(id));
    return response.data.data;
  },

  async unarchiveProject(id) {
    const response = await api.post(API_ENDPOINTS.PROJECTS.UNARCHIVE(id));
    return response.data.data;
  },
};

export default projectService;
