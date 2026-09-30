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
      // silently fail
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
      // silently fail
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationService.markAllAsRead();
      loadNotifications();
      onUpdate();
    } catch (err) {
      // silently fail
    }
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: '2.75rem',
        right: 0,
        width: '380px',
        backgroundColor: 'var(--bg-surface)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        boxShadow: 'var(--shadow-lg)',
        zIndex: 100,
        padding: '16px',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
        <h4 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>Notifications</h4>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button onClick={handleMarkAllRead} className="btn btn-secondary btn-sm" title="Mark all read">
            <CheckCheck size={14} /> Mark all read
          </button>
          <button onClick={onClose} style={{ color: 'var(--text-muted)', padding: '4px' }} title="Close">
            <X size={16} />
          </button>
        </div>
      </div>

      {loading ? (
        <div style={{ padding: '16px', textAlign: 'center', color: 'var(--text-muted)' }}>Loading...</div>
      ) : notifications.length === 0 ? (
        <div style={{ padding: '32px 16px', textAlign: 'center', color: 'var(--text-muted)' }}>
          <BellOff size={24} style={{ marginBottom: '8px' }} />
          <div>No notifications</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '340px', overflowY: 'auto' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              style={{
                padding: '12px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: n.status === 'UNREAD' ? 'rgba(15, 32, 82, 0.04)' : 'var(--bg-primary)',
                border: '1px solid var(--border-color)',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <span style={{ fontWeight: 600, fontSize: '14px', color: 'var(--text-primary)' }}>{n.title}</span>
                {n.status === 'UNREAD' && (
                  <button
                    onClick={() => handleMarkAsRead(n.id)}
                    style={{ fontSize: '12px', color: 'var(--primary-color)', fontWeight: 600 }}
                  >
                    Mark read
                  </button>
                )}
              </div>
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>{n.message}</p>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block', marginTop: '6px' }}>
                {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
