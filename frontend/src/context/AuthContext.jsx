import React, { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/auth.service';
import websocketService from '../services/websocket.service';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = () => {
      const currentUser = authService.getCurrentUser();
      setUser(currentUser);
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (credentials) => {
    const response = await authService.login(credentials);
    setUser(response.user);

    // Connect WebSocket
    if (response.user?.id) {
      try {
        await websocketService.connect(response.user.id, (notification) => {
          // Handle real-time notifications
          console.log('Received notification:', notification);
        });
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
      }
    }

    return response;
  };

  const register = async (userData) => {
    const response = await authService.register(userData);
    setUser(response.user);

    // Connect WebSocket
    if (response.user?.id) {
      try {
        await websocketService.connect(response.user.id, (notification) => {
          console.log('Received notification:', notification);
        });
      } catch (error) {
        console.error('Failed to connect WebSocket:', error);
      }
    }

    return response;
  };

  const logout = () => {
    authService.logout();
    websocketService.disconnect();
    setUser(null);
  };

  const updateUser = (updatedUser) => {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    updateUser,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
