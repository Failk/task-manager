import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { useNotificationStore } from '../store/notificationStore';
import type { Notification as AppNotification } from '../types';

class WebSocketService {
  private client: Client | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  connect(token: string): void {
    if (this.client?.connected) {
      return;
    }

    // Use relative URL for Docker deployment (nginx proxy) or fallback to localhost for dev
    const wsUrl = import.meta.env.VITE_WS_URL || '/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        if (import.meta.env.DEV) {
          console.log('STOMP:', str);
        }
      },
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
      this.subscribeToNotifications();
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Details:', frame.body);
    };

    this.client.onWebSocketClose = () => {
      console.log('WebSocket closed');
      this.handleReconnect(token);
    };

    this.client.activate();
  }

  private handleReconnect(token: string): void {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Reconnecting... Attempt ${this.reconnectAttempts}`);
      setTimeout(() => {
        this.connect(token);
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('Max reconnection attempts reached');
    }
  }

  private subscribeToNotifications(): void {
    if (!this.client?.connected) return;

    // Subscribe to user-specific notifications
    this.client.subscribe('/user/queue/notifications', (message: IMessage) => {
      this.handleNotification(message);
    });

    // Subscribe to task reminders
    this.client.subscribe('/user/queue/reminders', (message: IMessage) => {
      this.handleReminder(message);
    });

    // Subscribe to task updates (for real-time sync)
    this.client.subscribe('/user/queue/tasks', (message: IMessage) => {
      this.handleTaskUpdate(message);
    });
  }

  private handleNotification(message: IMessage): void {
    try {
      const notification: AppNotification = JSON.parse(message.body);
      useNotificationStore.getState().addNotification(notification);
      
      // Show browser notification if permitted
      this.showBrowserNotification(notification);
    } catch (error) {
      console.error('Error parsing notification:', error);
    }
  }

  private handleReminder(message: IMessage): void {
    try {
      const reminder = JSON.parse(message.body);
      const notification: AppNotification = {
        id: `reminder-${Date.now()}`,
        type: 'REMINDER',
        title: 'Task Reminder',
        message: reminder.message || `Reminder for task: ${reminder.taskTitle}`,
        taskId: reminder.taskId,
        read: false,
        createdAt: new Date().toISOString(),
      };
      useNotificationStore.getState().addNotification(notification);
      this.showBrowserNotification(notification);
    } catch (error) {
      console.error('Error parsing reminder:', error);
    }
  }

  private handleTaskUpdate(message: IMessage): void {
    try {
      const update = JSON.parse(message.body);
      // Emit custom event for components to listen
      window.dispatchEvent(new CustomEvent('taskUpdate', { detail: update }));
    } catch (error) {
      console.error('Error parsing task update:', error);
    }
  }

  private async showBrowserNotification(notification: AppNotification): Promise<void> {
    if (!('Notification' in window)) return;

    if (Notification.permission === 'granted') {
      new window.Notification(notification.title, {
        body: notification.message,
        icon: '/favicon.ico',
        tag: notification.id,
      });
    } else if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission();
      if (permission === 'granted') {
        new window.Notification(notification.title, {
          body: notification.message,
          icon: '/favicon.ico',
          tag: notification.id,
        });
      }
    }
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.reconnectAttempts = 0;
    }
  }

  isConnected(): boolean {
    return this.client?.connected ?? false;
  }

  // Send a message through WebSocket (if needed)
  send(destination: string, body: object): void {
    if (this.client?.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
      });
    }
  }
}

export const webSocketService = new WebSocketService();
