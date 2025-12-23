import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const taskService = {
  async getAllTasks() {
    const response = await api.get(API_ENDPOINTS.TASKS.LIST);
    return response.data.data || [];
  },

  async getTodayTasks() {
    const response = await api.get(API_ENDPOINTS.TASKS.TODAY);
    return response.data.data || [];
  },

  async getOverdueTasks() {
    const response = await api.get(API_ENDPOINTS.TASKS.OVERDUE);
    return response.data.data || [];
  },

  async getTask(id) {
    const response = await api.get(API_ENDPOINTS.TASKS.GET(id));
    return response.data.data;
  },

  async filterTasks(filters) {
    const response = await api.post(API_ENDPOINTS.TASKS.FILTER, filters);
    return response.data.data || [];
  },

  async createOneTimeTask(taskData) {
    const response = await api.post(API_ENDPOINTS.TASKS.CREATE_ONE_TIME, taskData);
    return response.data.data;
  },

  async createRecurringTask(taskData) {
    const response = await api.post(API_ENDPOINTS.TASKS.CREATE_RECURRING, taskData);
    return response.data.data;
  },

  async updateTask(id, taskData) {
    const response = await api.put(API_ENDPOINTS.TASKS.UPDATE(id), taskData);
    return response.data.data;
  },

  async deleteTask(id) {
    await api.delete(API_ENDPOINTS.TASKS.DELETE(id));
  },

  async completeTask(id) {
    const response = await api.post(API_ENDPOINTS.TASKS.COMPLETE(id));
    return response.data.data;
  },

  async updateTaskStatus(id, status) {
    const response = await api.put(
      API_ENDPOINTS.TASKS.UPDATE_STATUS(id),
      null,
      { params: { status } }
    );
    return response.data.data;
  },

  async getTasksByProject(projectId) {
    const response = await api.get(API_ENDPOINTS.TASKS.BY_PROJECT(projectId));
    return response.data.data || [];
  },

  async getTasksByContext(contextId) {
    const response = await api.get(API_ENDPOINTS.TASKS.BY_CONTEXT(contextId));
    return response.data.data || [];
  },
};

export default taskService;
