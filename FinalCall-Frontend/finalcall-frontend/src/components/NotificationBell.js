// src/components/NotificationBell.js
import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell } from 'lucide-react';
import { NotificationContext } from '../context/NotificationContext';
import { formatDistance } from 'date-fns';

const NotificationBell = () => {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useContext(NotificationContext);
  const [isOpen, setIsOpen] = useState(false);
  const navigate = useNavigate();

  const toggleDropdown = () => {
    setIsOpen(prev => !prev);
  };

  const handleNotificationClick = (notification) => {
    if (!notification.read) {
      markAsRead(notification.id);
    }
    setIsOpen(false);
    if (notification.link) {
      navigate(notification.link);
    }
  };

  const handleMarkAllAsRead = () => {
    markAllAsRead();
  };

  return (
    <div className="relative">
      <button
        onClick={toggleDropdown}
        className="relative p-2 text-gray-200 hover:text-white focus:outline-none"
        aria-label="Notifications"
      >
        <Bell className="h-6 w-6" />
        {unreadCount > 0 && (
          <span className="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-500 rounded-full">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 bg-white rounded-md shadow-lg overflow-hidden z-50">
          <div className="py-2">
            <div className="flex justify-between items-center px-4 py-2 border-b">
              <h3 className="text-lg font-semibold text-gray-800">Notifications</h3>
              {unreadCount > 0 && (
                <button
                  onClick={handleMarkAllAsRead}
                  className="text-sm text-blue-500 hover:text-blue-700"
                >
                  Mark all as read
                </button>
              )}
            </div>
            <div className="max-h-96 overflow-y-auto">
              {notifications.length === 0 ? (
                <p className="px-4 py-2 text-gray-500">No notifications</p>
              ) : (
                notifications.map(notification => (
                  <div
                    key={notification.id}
                    className={`px-4 py-2 hover:bg-gray-50 cursor-pointer ${
                      !notification.read ? 'bg-blue-50' : ''
                    }`}
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <p className={`text-sm text-gray-800 ${!notification.read ? 'font-bold' : ''}`}>
                      {notification.message}
                    </p>
                    <p className="text-xs text-gray-500 mt-1">
                      {formatDistance(new Date(notification.timestamp), new Date(), {
                        addSuffix: true
                      })}
                    </p>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
