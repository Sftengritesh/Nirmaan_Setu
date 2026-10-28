import React, { useEffect, useState } from 'react';
import { apiClient } from '../../services/apiClient';
import { NotificationItem } from '../../types/schema';
import { X, BellOff, Clock, Check } from 'lucide-react';

interface NotificationDrawerProps {
  userId: string;
  onClose: () => void;
  onUpdate: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({ onClose, onUpdate }) => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await apiClient.notifications.getMyNotifications();
      const list = Array.isArray(data) ? data : (data as any)?.content || [];
      const mapped: NotificationItem[] = list.map((item: any) => ({
        id: item.id,
        userId: item.recipientUserId || '',
        title: item.title || item.type || 'Notification',
        body: item.message || '',
        type: item.type || 'ALERT',
        read: item.status === 'READ',
        createdAt: item.createdAt || new Date().toISOString(),
      }));
      setNotifications(mapped);
    } catch (err) {
      console.warn('Failed to load notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAsRead = async (id: string) => {
    try {
      await apiClient.notifications.markAsRead(id);
      setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
      onUpdate();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await apiClient.notifications.markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      onUpdate();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: '2.75rem',
        right: 0,
        width: '360px',
        backgroundColor: 'var(--bg-surface)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        boxShadow: 'var(--shadow-lg)',
        zIndex: 100,
        padding: '16px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
        <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}>
          Notifications
        </h4>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          {notifications.some((n) => !n.read) && (
            <button
              onClick={handleMarkAllRead}
              style={{
                background: 'none',
                border: 'none',
                color: 'var(--primary-color)',
                fontSize: '12px',
                cursor: 'pointer',
                fontWeight: 600,
              }}
            >
              Mark all read
            </button>
          )}
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: '4px' }}
          >
            <X size={16} />
          </button>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '16px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          Loading notifications...
        </div>
      ) : notifications.length === 0 ? (
        <div style={{ padding: '32px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
          <BellOff size={24} style={{ marginBottom: '8px' }} />
          <div style={{ fontSize: '0.875rem' }}>No new notifications</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '340px', overflowY: 'auto' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              style={{
                padding: '10px 12px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: !n.read ? 'rgba(15, 32, 82, 0.05)' : 'var(--bg-primary)',
                border: !n.read ? '1px solid rgba(15, 32, 82, 0.15)' : '1px solid var(--border-color)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span style={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--text-primary)' }}>{n.title}</span>
                {!n.read && (
                  <button
                    onClick={() => handleMarkAsRead(n.id)}
                    style={{
                      fontSize: '0.75rem',
                      color: 'var(--primary-color)',
                      cursor: 'pointer',
                      background: 'none',
                      border: 'none',
                      fontWeight: 600,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '2px',
                    }}
                  >
                    <Check size={12} /> Mark Read
                  </button>
                )}
              </div>
              <p style={{ margin: '4px 0 6px 0', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{n.body}</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                <Clock size={12} />
                <span>{new Date(n.createdAt).toLocaleDateString()}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
