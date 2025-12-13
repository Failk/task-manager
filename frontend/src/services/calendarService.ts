import api from './api';
import { CalendarView } from '../types';

export const calendarService = {
  getDailyView: async (date: string): Promise<CalendarView> => {
    const response = await api.get<CalendarView>('/calendar/daily', {
      params: { date },
    });
    return response.data;
  },

  getWeeklyView: async (date: string): Promise<CalendarView> => {
    const response = await api.get<CalendarView>('/calendar/weekly', {
      params: { date },
    });
    return response.data;
  },

  getMonthlyView: async (year: number, month: number): Promise<CalendarView> => {
    const response = await api.get<CalendarView>('/calendar/monthly', {
      params: { year, month },
    });
    return response.data;
  },
};
