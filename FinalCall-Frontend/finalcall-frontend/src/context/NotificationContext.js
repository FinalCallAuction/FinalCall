import React, { createContext, useState, useEffect, useContext } from 'react';
import { AuthContext } from './AuthContext';

export const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const { user } = useContext(AuthContext);

  useEffect(() => {
    if (!user || !user.id) {
      console.log('No user logged in, not connecting to notifications');
      return;
    }

    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws';
    const ws = new WebSocket(`${wsProtocol}://${window.location.host}/ws/notifications/${user.id}`);

    ws.onopen = () => {
      console.log('Notifications WebSocket connected');
    };

    ws.onmessage = (event) => {
      const notification = JSON.parse(event.data);
      
      // Handle different notification types
      switch(notification.type) {
        case 'NEW_ITEM':
          // Dispatch custom event for ItemsPage
          window.dispatchEvent(new CustomEvent('refreshItems'));
          break;
        case 'NEW_BID':
          // Dispatch event for seller's items
          window.dispatchEvent(new CustomEvent('refreshUserItems'));
          break;
        case 'OUTBID':
          // Dispatch event to refresh user's bids
          window.dispatchEvent(new CustomEvent('refreshUserBids'));
          break;
        case 'AUCTION_WON':
        case 'AUCTION_ENDED':
          // Refresh both items and bids
          window.dispatchEvent(new CustomEvent('refreshItems'));
          window.dispatchEvent(new CustomEvent('refreshUserBids'));
          break;
        default:
          break;
      }

      setNotifications(prev => [notification, ...prev]);
      setUnreadCount(prev => prev + 1);
    };

    ws.onclose = () => {
      console.log('Notifications WebSocket disconnected');
    };

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [user]);

  const markAsRead = (notificationId) => {
    setNotifications(prev =>
      prev.map(notif =>
        notif.id === notificationId ? { ...notif, read: true } : notif
      )
    );
    setUnreadCount(prev => Math.max(0, prev - 1));
  };

  const markAllAsRead = () => {
    setNotifications(prev =>
      prev.map(notif => ({ ...notif, read: true }))
    );
    setUnreadCount(0);
  };

  return (
    <NotificationContext.Provider value={{
      notifications,
      unreadCount,
      markAsRead,
      markAllAsRead
    }}>
      {children}
    </NotificationContext.Provider>
  );
};