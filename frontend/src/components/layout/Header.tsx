import React, { useState } from 'react';
import { CurrentUserResponse } from '../../services/authService';
import { Bell, User, LogOut, ShieldCheck, HardHat, Briefcase, Building } from 'lucide-react';
import { NotificationDrawer } from './NotificationDrawer';

interface HeaderProps {
  currentUser: CurrentUserResponse | null;
  activeRole: string;
  onSelectRole: (role: string) => void;
  onLogout: () => void;
  unreadCount: number;
  onNotificationRead: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  currentUser,
  activeRole,
  onSelectRole,
  onLogout,
  unreadCount,
  onNotificationRead,
}) => {
  const [showNotifications, setShowNotifications] = useState(false);

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'CLIENT': return <Building size={14} />;
      case 'WORKER': return <HardHat size={14} />;
      case 'CONTRACTOR': return <Briefcase size={14} />;
      case 'ADMIN': return <ShieldCheck size={14} />;
      default: return <User size={14} />;
    }
  };

  return (
    <header className="navbar">
      <div className="brand-logo">
        <span style={{ color: 'var(--primary-color)' }}>Nirmaan</span>Setu
        <span className="brand-badge">MVP</span>
      </div>

      {currentUser && (
        <div className="nav-actions">
          {/* Active Role Selector */}
          <div style={{ display: 'flex', gap: '0.375rem', alignItems: 'center' }}>
            {currentUser.roles.map((role) => {
              const isActive = activeRole === role;
              return (
                <button
                  key={role}
                  onClick={() => onSelectRole(role)}
                  className={`badge badge-${role.toLowerCase()}`}
                  style={{
                    opacity: isActive ? 1 : 0.5,
                    border: isActive ? '1px solid currentColor' : '1px solid transparent',
                    cursor: 'pointer',
                  }}
                >
                  {getRoleIcon(role)}
                  {role}
                </button>
              );
            })}
          </div>

          {/* Notifications Bell */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="btn btn-secondary btn-sm"
              style={{ position: 'relative', padding: '0.5rem' }}
            >
              <Bell size={18} />
              {unreadCount > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: '-4px',
                    right: '-4px',
                    backgroundColor: 'var(--accent-amber)',
                    color: '#000',
                    fontSize: '0.6875rem',
                    fontWeight: 700,
                    borderRadius: '9999px',
                    padding: '0.1rem 0.35rem',
                  }}
                >
                  {unreadCount}
                </span>
              )}
            </button>

            {showNotifications && (
              <NotificationDrawer
                onClose={() => setShowNotifications(false)}
                onUpdate={onNotificationRead}
              />
            )}
          </div>

          {/* Phone Number & Logout */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '0.875rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>{currentUser.phoneNumber}</span>
            <button onClick={onLogout} className="btn btn-secondary btn-sm" title="Log out">
              <LogOut size={16} />
              Exit
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
