import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const calendarService = {
  async getDailyView(date) {
    const response = await api.get(API_ENDPOINTS.CALENDAR.DAILY, {
      params: { date },
    });
    return response.data.data;
  },

  async getWeeklyView(date) {
    const response = await api.get(API_ENDPOINTS.CALENDAR.WEEKLY, {
      params: { date },
    });
    return response.data.data;
  },

  async getMonthlyView(date) {
    const response = await api.get(API_ENDPOINTS.CALENDAR.MONTHLY, {
      params: { date },
    });
    return response.data.data;
  },
};

export default calendarService;
