import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { FiX, FiBell, FiCheck, FiTrash2 } from 'react-icons/fi';
import { formatDistanceToNow } from 'date-fns';
import notificationService from '../../services/notification.service';

const NotificationPanel = ({ isOpen, onClose }) => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('ALL');

  // Load notifications based on filter parameter (not state)
  const loadNotifications = async (filterType) => {
    try {
      setLoading(true);
      let data;
      if (filterType === 'UNREAD') {
        data = await notificationService.getUnreadNotifications();
      } else {
        data = await notificationService.getAllNotifications();
      }
      setNotifications(data);
    } catch (error) {
      toast.error('Failed to load notifications');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  // Load on open or filter change
  useEffect(() => {
    if (isOpen) {
      loadNotifications(filter);
    }
  }, [isOpen, filter]);

  const handleFilterChange = (newFilter) => {
    if (newFilter !== filter) {
      setFilter(newFilter);
      // useEffect will trigger loadNotifications
    }
  };

  const handleAcknowledge = async (notification) => {
    try {
      // Optimistically update UI
      setNotifications(prev =>
        prev.map(n => n.id === notification.id ? { ...n, read: true } : n)
      );

      await notificationService.acknowledgeNotification(notification.id);
      toast.success('Notification marked as read');

      // Reload to get fresh data
      await loadNotifications(filter);
    } catch (error) {
      // Revert on failure
      setNotifications(prev =>
        prev.map(n => n.id === notification.id ? { ...n, read: false } : n)
      );
      toast.error('Failed to acknowledge notification');
    }
  };

  const handleDelete = async (notification) => {
    try {
      setNotifications(prev => prev.filter(n => n.id !== notification.id));
      await notificationService.deleteNotification(notification.id);
      toast.success('Notification deleted');
    } catch (error) {
      await loadNotifications(filter);
      toast.error('Failed to delete notification');
    }
  };



  if (!isOpen) return null;

  return (
    <>
      <div className="fixed inset-0 bg-black bg-opacity-50 z-40" onClick={onClose} />
      <div className="fixed right-0 top-0 h-full w-full max-w-md bg-white shadow-xl z-50 flex flex-col">
        <div className="flex items-center justify-between p-6 border-b">
          <div className="flex items-center gap-2">
            <FiBell size={24} className="text-primary-600" />
            <h2 className="text-xl font-bold">Notifications</h2>
            {notifications.filter(n => !n.read).length > 0 && (
              <span className="text-sm text-gray-500">
                ({notifications.filter(n => !n.read).length} unread)
              </span>
            )}
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <FiX size={24} />
          </button>
        </div>

        <div className="flex gap-2 p-4 border-b">
          <button
            onClick={() => handleFilterChange('ALL')}
            className={`px-4 py-2 rounded-lg text-sm transition-colors ${filter === 'ALL' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
          >
            All
          </button>
          <button
            onClick={() => handleFilterChange('UNREAD')}
            className={`px-4 py-2 rounded-lg text-sm transition-colors ${filter === 'UNREAD' ? 'bg-primary-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
          >
            Unread
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {loading ? (
            <div className="flex items-center justify-center h-32">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            </div>
          ) : notifications.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <FiBell size={48} className="mx-auto mb-4 text-gray-300" />
              <p>{filter === 'UNREAD' ? 'No unread notifications' : 'No notifications'}</p>
            </div>
          ) : (
            <div className="divide-y">
              {notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`p-4 hover:bg-gray-50 transition-colors ${!notification.read ? 'bg-blue-50' : ''
                    }`}
                >
                  <div className="flex items-start gap-3">
                    <div className={`mt-1 w-2 h-2 rounded-full flex-shrink-0 ${!notification.read ? 'bg-blue-600' : 'bg-gray-300'
                      }`} />

                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="font-medium text-gray-900 mb-1">{notification.title}</h3>
                        <button
                          onClick={() => handleDelete(notification)}
                          className="p-1 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors flex-shrink-0"
                          title="Delete notification"
                        >
                          <FiTrash2 size={14} />
                        </button>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">{notification.message}</p>

                      <div className="flex items-center gap-4 text-xs text-gray-500">
                        <span>
                          {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })}
                        </span>
                        {notification.taskTitle && (
                          <span className="text-primary-600">
                            📋 {notification.taskTitle}
                          </span>
                        )}
                      </div>

                      {!notification.read && (
                        <div className="flex gap-2 mt-3">
                          <button
                            onClick={() => handleAcknowledge(notification)}
                            className="text-xs px-3 py-1 bg-primary-100 text-primary-700 rounded-lg hover:bg-primary-200 flex items-center gap-1 transition-colors"
                          >
                            <FiCheck size={12} />
                            Mark as read
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default NotificationPanel;

