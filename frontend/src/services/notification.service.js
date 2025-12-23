import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const notificationService = {
  async getAllNotifications() {
    const response = await api.get(API_ENDPOINTS.NOTIFICATIONS.LIST);
    return response.data.data || [];
  },

  async getUnreadNotifications() {
    const response = await api.get(API_ENDPOINTS.NOTIFICATIONS.UNREAD);
    return response.data.data || [];
  },

  async getUnreadCount() {
    const response = await api.get(API_ENDPOINTS.NOTIFICATIONS.COUNT);
    return response.data.data?.unreadCount || 0;
  },

  async acknowledgeNotification(id) {
    const response = await api.post(API_ENDPOINTS.NOTIFICATIONS.ACKNOWLEDGE(id));
    return response.data.data;
  },

  async snoozeNotification(id, minutes = 15) {
    const response = await api.post(
      API_ENDPOINTS.NOTIFICATIONS.SNOOZE(id),
      null,
      { params: { minutes } }
    );
    return response.data.data;
  },
};

export default notificationService;
