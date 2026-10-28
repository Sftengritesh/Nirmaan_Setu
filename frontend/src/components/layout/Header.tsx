import React, { useState, useEffect } from 'react';
import { Bell, User, LogOut, ShieldCheck, HardHat, Building, Briefcase } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { UserRole } from '../../types/schema';
import { NotificationDrawer } from './NotificationDrawer';
import { apiClient } from '../../services/apiClient';

export const Header: React.FC = () => {
  const { currentUser, userProfile, activeRole, setActiveRole, logout } = useAuth();
  const [showNotifications, setShowNotifications] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  const fetchUnread = async () => {
    if (currentUser) {
      try {
        const count = await apiClient.notifications.getUnreadCount();
        setUnreadCount(count);
      } catch (_) {
        // quiet ignore if unauthenticated or network failure
      }
    }
  };

  useEffect(() => {
    fetchUnread();
    const interval = setInterval(fetchUnread, 30000); // refresh every 30s
    return () => clearInterval(interval);
  }, [currentUser]);

  const getRoleIcon = (role: UserRole) => {
    switch (role) {
      case 'CLIENT':
        return <Building size={14} />;
      case 'WORKER':
        return <HardHat size={14} />;
      case 'ADMIN':
        return <ShieldCheck size={14} />;
      case 'CONTRACTOR':
        return <Briefcase size={14} />;
      default:
        return <User size={14} />;
    }
  };

  // Determine available roles for switcher:
  // If user has ADMIN role, allow viewing any perspective for administration/testing.
  // Otherwise, allow their specific assigned roles.
  const userRoles = currentUser?.roles || [];
  const isAdmin = userRoles.includes('ADMIN');
  const availableRoles: UserRole[] = isAdmin
    ? ['ADMIN', 'WORKER', 'CONTRACTOR', 'CLIENT']
    : (userRoles.length > 0 ? userRoles : [activeRole]);

  return (
    <header className="navbar">
      <div className="brand-logo" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        <div>
          <span style={{ color: '#B6C4FF', fontWeight: 800 }}>Nirmaan</span>
          <span style={{ color: '#FFFFFF', fontWeight: 800 }}>Setu</span>
        </div>
        <span
          style={{
            fontSize: '10px',
            backgroundColor: '#16A34A',
            color: '#FFFFFF',
            padding: '2px 6px',
            borderRadius: '4px',
            fontWeight: 700,
            letterSpacing: '0.5px',
          }}
        >
          PROD
        </span>
      </div>

      {currentUser && (
        <div className="nav-actions" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {/* Active Role Selector */}
          <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
            {availableRoles.map((role) => {
              const isActive = activeRole === role;
              return (
                <button
                  key={role}
                  onClick={() => setActiveRole(role)}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: '4px 10px',
                    borderRadius: '9999px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    backgroundColor: isActive ? 'rgba(255,255,255,0.22)' : 'rgba(255,255,255,0.08)',
                    color: isActive ? '#FFFFFF' : 'rgba(255,255,255,0.7)',
                    border: isActive ? '1px solid rgba(255,255,255,0.4)' : '1px solid transparent',
                    transition: 'all 0.2s ease',
                  }}
                  title={`Switch to ${role} perspective`}
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
                    fontSize: '10px',
                    fontWeight: 'bold',
                    padding: '2px 5px',
                    borderRadius: '9999px',
                    lineHeight: 1,
                  }}
                >
                  {unreadCount}
                </span>
              )}
            </button>

            {showNotifications && (
              <NotificationDrawer
                userId={currentUser.uid}
                onClose={() => setShowNotifications(false)}
                onUpdate={() => {
                  fetchUnread();
                }}
              />
            )}
          </div>

          {/* User Profile & Logout */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#FFFFFF', fontSize: '0.85rem' }}>
              <div
                style={{
                  width: '28px',
                  height: '28px',
                  borderRadius: '50%',
                  backgroundColor: 'rgba(255,255,255,0.2)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontWeight: 600,
                  fontSize: '12px',
                }}
              >
                {userProfile?.displayName ? userProfile.displayName.charAt(0).toUpperCase() : 'U'}
              </div>
              <span style={{ fontSize: '13px', fontWeight: 500 }}>
                {userProfile?.displayName || currentUser.phone}
              </span>
            </div>

            <button
              onClick={logout}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
                padding: '6px 10px',
                borderRadius: 'var(--radius-sm)',
                backgroundColor: 'rgba(239, 68, 68, 0.2)',
                color: '#FCA5A5',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
              title="Sign Out"
            >
              <LogOut size={14} /> Sign Out
            </button>
          </div>
        </div>
      )}
    </header>
  );
};
