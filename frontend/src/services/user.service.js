import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const userService = {
  async getProfile() {
    const response = await api.get(API_ENDPOINTS.USERS.ME);
    return response.data.data;
  },

  async updateProfile(userData) {
    const response = await api.put(API_ENDPOINTS.USERS.UPDATE_PROFILE, userData);
    localStorage.setItem('user', JSON.stringify(response.data.data));
    return response.data.data;
  },

  async changePassword(passwordData) {
    await api.put(API_ENDPOINTS.USERS.CHANGE_PASSWORD, passwordData);
  },

  async getNotificationPreferences() {
    const response = await api.get(API_ENDPOINTS.USERS.NOTIFICATIONS_PREFERENCES);
    return response.data.data;
  },

  async updateNotificationPreferences(preferences) {
    const response = await api.put(
      API_ENDPOINTS.USERS.NOTIFICATIONS_PREFERENCES,
      preferences
    );
    return response.data.data;
  },
};

export default userService;
