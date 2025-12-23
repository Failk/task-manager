import api from './api.service';
import { API_ENDPOINTS } from '../config/api';

const authService = {
  async register(userData) {
    const response = await api.post(API_ENDPOINTS.AUTH.REGISTER, userData);
    console.log('Register API response:', response.data);

    // Backend returns: { success, message, data: { accessToken, refreshToken, user, ... } }
    const authData = response.data.data;
    if (authData.accessToken) {
      localStorage.setItem('token', authData.accessToken);
      localStorage.setItem('refreshToken', authData.refreshToken);
      localStorage.setItem('user', JSON.stringify(authData.user));
    }
    return authData;
  },

  async login(credentials) {
    const response = await api.post(API_ENDPOINTS.AUTH.LOGIN, credentials);
    console.log('Login API response:', response.data);

    // Backend returns: { success, message, data: { accessToken, refreshToken, user, ... } }
    const authData = response.data.data;
    if (authData.accessToken) {
      localStorage.setItem('token', authData.accessToken);
      localStorage.setItem('refreshToken', authData.refreshToken);
      localStorage.setItem('user', JSON.stringify(authData.user));
    }
    return authData;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!localStorage.getItem('token');
  },
};

export default authService;
