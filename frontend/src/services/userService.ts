import api from './api';
import { User } from '../types';

interface UpdateProfileData {
  fullName?: string;
  username?: string;
  email?: string;
}

interface ChangePasswordData {
  currentPassword: string;
  newPassword: string;
}

interface NotificationPreferences {
  emailReminders: boolean;
  emailDueDates: boolean;
  emailOverdue: boolean;
  pushReminders: boolean;
  pushDueDates: boolean;
  pushOverdue: boolean;
  reminderTime: string;
}

export const userService = {
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get('/users/me');
    return response.data;
  },

  updateProfile: async (data: UpdateProfileData): Promise<User> => {
    const response = await api.put('/users/me', data);
    return response.data;
  },

  changePassword: async (data: ChangePasswordData): Promise<void> => {
    await api.post('/users/me/password', data);
  },

  getNotificationPreferences: async (): Promise<NotificationPreferences> => {
    const response = await api.get('/users/me/notifications');
    return response.data;
  },

  updateNotificationPreferences: async (
    preferences: NotificationPreferences
  ): Promise<NotificationPreferences> => {
    const response = await api.put('/users/me/notifications', preferences);
    return response.data;
  },

  deleteAccount: async (): Promise<void> => {
    await api.delete('/users/me');
  },
};
