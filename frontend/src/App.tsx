import React, { useEffect, useState } from 'react';
import { authService, CurrentUserResponse, AuthResponse } from './services/authService';
import { notificationService } from './services/notificationService';
import { Header } from './components/layout/Header';
import { LoginPage } from './pages/auth/LoginPage';
import { DashboardPage } from './pages/dashboard/DashboardPage';

export const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<CurrentUserResponse | null>(null);
  const [activeRole, setActiveRole] = useState<string>('CLIENT');
  const [loading, setLoading] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadSession = async () => {
    setLoading(true);
    try {
      const user = await authService.getMe();
      setCurrentUser(user);
      if (user.roles && user.roles.length > 0) {
        setActiveRole(user.roles[0]);
      }
      fetchUnreadCount();
    } catch (err) {
      setCurrentUser(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const res = await notificationService.getUnreadCount();
      setUnreadCount(res.unreadCount);
    } catch (err) {
      // Ignore if not logged in
    }
  };

  useEffect(() => {
    loadSession();
  }, []);

  // Poll unread notifications count every 30 seconds
  useEffect(() => {
    if (!currentUser) return;
    const interval = setInterval(fetchUnreadCount, 30000);
    return () => clearInterval(interval);
  }, [currentUser]);

  const handleLoginSuccess = (authData: AuthResponse) => {
    const user: CurrentUserResponse = {
      userId: authData.userId,
      phoneNumber: authData.phoneNumber,
      roles: authData.roles,
    };
    setCurrentUser(user);
    if (user.roles.length > 0) {
      setActiveRole(user.roles[0]);
    }
    fetchUnreadCount();
  };

  const handleLogout = async () => {
    await authService.logout();
    setCurrentUser(null);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        Loading NirmaanSetu application...
      </div>
    );
  }

  return (
    <div className="app-container">
      <Header
        currentUser={currentUser}
        activeRole={activeRole}
        onSelectRole={(role) => setActiveRole(role)}
        onLogout={handleLogout}
        unreadCount={unreadCount}
        onNotificationRead={fetchUnreadCount}
      />

      <main className="main-content">
        {!currentUser ? (
          <LoginPage onLoginSuccess={handleLoginSuccess} />
        ) : (
          <DashboardPage currentUser={currentUser} activeRole={activeRole} />
        )}
      </main>
    </div>
  );
};

export default App;
