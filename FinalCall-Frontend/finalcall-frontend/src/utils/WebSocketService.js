// src/utils/WebSocketService.js
export class WebSocketService {
  constructor() {
    this.connections = {};
    this.notificationConnections = {};
    this.messageHandlers = new Set();
    this.notificationHandlers = new Set();
    this.auctionHandlers = new Set();
  }

  connectNotifications(userId) {
    if (!this.notificationConnections[userId]) {
      const ws = new WebSocket(`ws://localhost:8084/ws/notifications/${userId}`);

      ws.onmessage = (event) => {
        const notification = JSON.parse(event.data);
        this.notificationHandlers.forEach(handler => handler(notification));
      };

      this.notificationConnections[userId] = ws;
    }
    return this.notificationConnections[userId];
  }

  addNotificationHandler(handler) {
    this.notificationHandlers.add(handler);
    return () => this.notificationHandlers.delete(handler);
  }

  removeNotificationHandler(handler) {
    this.notificationHandlers.delete(handler);
  }

  disconnectNotifications(userId) {
    if (this.notificationConnections[userId]) {
      this.notificationConnections[userId].close();
      delete this.notificationConnections[userId];
    }
  }
}

export const webSocketService = new WebSocketService();
