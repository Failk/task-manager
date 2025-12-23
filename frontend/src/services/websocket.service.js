import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE_URL } from '../config/api';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect(userId, onNotification) {
    if (this.connected) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('token');

      this.client = new Client({
        webSocketFactory: () => new SockJS(WS_BASE_URL),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          console.log('STOMP: ' + str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = () => {
        console.log('WebSocket connected');
        this.connected = true;

        // Subscribe to user notifications
        const subscription = this.client.subscribe(
          `/user/${userId}/queue/notifications`,
          (message) => {
            const notification = JSON.parse(message.body);
            onNotification(notification);
          }
        );

        this.subscriptions.set('notifications', subscription);
        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error('STOMP error', frame);
        this.connected = false;
        reject(new Error('WebSocket connection error'));
      };

      this.client.onWebSocketError = (error) => {
        console.error('WebSocket error', error);
        this.connected = false;
        reject(error);
      };

      this.client.onDisconnect = () => {
        console.log('WebSocket disconnected');
        this.connected = false;
      };

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client && this.connected) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      this.client.deactivate();
      this.connected = false;
    }
  }

  isConnected() {
    return this.connected;
  }
}

export default new WebSocketService();
