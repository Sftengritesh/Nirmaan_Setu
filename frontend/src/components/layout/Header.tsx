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
        <span style={{ color: '#B6C4FF' }}>Nirmaan</span>Setu
        <span className="brand-badge">MVP</span>
      </div>

      {currentUser && (
        <div className="nav-actions">
          {/* Active Role Selector */}
          <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
            {currentUser.roles.map((role) => {
              const isActive = activeRole === role;
              return (
                <button
                  key={role}
                  onClick={() => onSelectRole(role)}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: '4px 10px',
                    borderRadius: '9999px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    backgroundColor: isActive ? 'rgba(255,255,255,0.2)' : 'rgba(255,255,255,0.08)',
                    color: isActive ? '#FFFFFF' : 'rgba(255,255,255,0.65)',
                    border: isActive ? '1px solid rgba(255,255,255,0.35)' : '1px solid transparent',
                    transition: 'all 0.2s ease',
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
              style={{
                position: 'relative',
                padding: '8px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'rgba(255,255,255,0.1)',
                color: '#FFFFFF',
                border: '1px solid rgba(255,255,255,0.15)',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
              }}
              title="Notifications"
            >
              <Bell size={18} />
              {unreadCount > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: '-4px',
                    right: '-4px',
                    backgroundColor: '#FBBF24',
                    color: '#1B1B1F',
                    fontSize: '11px',
                    fontWeight: 700,
                    borderRadius: '9999px',
                    padding: '1px 5px',
                    lineHeight: '14px',
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
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '14px' }}>
            <span style={{ color: 'rgba(255,255,255,0.7)' }}>{currentUser.phoneNumber}</span>
            <button
              onClick={onLogout}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '6px',
                padding: '6px 12px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '13px',
                fontWeight: 600,
                backgroundColor: 'rgba(255,255,255,0.1)',
                color: '#FFFFFF',
                border: '1px solid rgba(255,255,255,0.15)',
                cursor: 'pointer',
              }}
              title="Log out"
            >
              <LogOut size={16} />
              Exit
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
