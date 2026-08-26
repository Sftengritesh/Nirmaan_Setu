import React, { useEffect, useState } from 'react';
import { notificationService, NotificationResponse } from '../../services/notificationService';
import { CheckCheck, X, BellOff } from 'lucide-react';

interface NotificationDrawerProps {
  onClose: () => void;
  onUpdate: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({ onClose, onUpdate }) => {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await notificationService.getMyNotifications(undefined, 0, 10);
      setNotifications(data.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationService.markAsRead(id);
      loadNotifications();
      onUpdate();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      loadNotifications();
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
        padding: '1rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
        <h4 style={{ fontSize: '1rem', fontWeight: 600 }}>Notifications</h4>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button onClick={handleMarkAllRead} className="btn btn-secondary btn-sm" title="Mark all read">
            <CheckCheck size={14} /> Mark all read
          </button>
          <button onClick={onClose} style={{ color: 'var(--text-muted)' }}><X size={16} /></button>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</div>
      ) : notifications.length === 0 ? (
        <div style={{ padding: '2rem 1rem', textAlign: 'center', color: 'var(--text-muted)' }}>
          <BellOff size={24} style={{ marginBottom: '0.5rem' }} />
          <div>No notifications</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '320px', overflowY: 'auto' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              style={{
                padding: '0.75rem',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: n.status === 'UNREAD' ? 'rgba(56, 189, 248, 0.08)' : 'var(--bg-card)',
                border: '1px solid var(--border-color)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>{n.title}</span>
                {n.status === 'UNREAD' && (
                  <button
                    onClick={() => handleMarkAsRead(n.id)}
                    style={{ fontSize: '0.75rem', color: 'var(--primary-color)' }}
                  >
                    Mark read
                  </button>
                )}
              </div>
              <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>{n.message}</p>
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)', display: 'block', marginTop: '0.375rem' }}>
                {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
