import React, { useState, useEffect } from 'react';
import { Capacitor } from '@capacitor/core';
import { App as CapacitorApp } from '@capacitor/app';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Header } from './components/layout/Header';
import { AuthPage } from './pages/auth/AuthPage';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { WorkerWorkspace } from './pages/worker/WorkerWorkspace';
import { ClientPortal } from './pages/client/ClientPortal';
import { ContractorPortal } from './pages/contractor/ContractorPortal';
import { PrivacyPolicy } from './pages/legal/PrivacyPolicy';
import { LoadingSpinner } from './components/common/LoadingSpinner';
import { Shield } from 'lucide-react';

const MainApp: React.FC = () => {
  const { currentUser, activeRole, loading } = useAuth();
  const [showPrivacyPolicy, setShowPrivacyPolicy] = useState(false);

  // Native Android hardware Back Button handler
  useEffect(() => {
    if (Capacitor.isNativePlatform()) {
      const backListener = CapacitorApp.addListener('backButton', ({ canGoBack }) => {
        if (showPrivacyPolicy) {
          setShowPrivacyPolicy(false);
        } else if (canGoBack) {
          window.history.back();
        } else {
          CapacitorApp.exitApp();
        }
      });

      return () => {
        backListener.then((handle) => handle.remove());
      };
    }
  }, [showPrivacyPolicy]);

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <LoadingSpinner message="Initializing NirmaanSetu Production Platform..." size={36} />
      </div>
    );
  }

  if (showPrivacyPolicy) {
    return (
      <div className="app-container">
        <Header />
        <main className="main-content">
          <PrivacyPolicy onBack={() => setShowPrivacyPolicy(false)} />
        </main>
      </div>
    );
  }

  return (
    <div className="app-container" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <main className="main-content" style={{ maxWidth: '1240px', width: '100%', margin: '0 auto', padding: '1.5rem 1rem', flex: 1, boxSizing: 'border-box' }}>
        {!currentUser ? (
          <AuthPage />
        ) : (
          <>
            {activeRole === 'ADMIN' && <AdminDashboard />}
            {activeRole === 'WORKER' && <WorkerWorkspace />}
            {activeRole === 'CLIENT' && <ClientPortal />}
            {activeRole === 'CONTRACTOR' && <ContractorPortal />}
          </>
        )}
      </main>

      {/* Production Footer with Play Store & Compliance Links */}
      <footer
        style={{
          borderTop: '1px solid var(--border-color)',
          backgroundColor: 'var(--bg-surface)',
          padding: '1rem',
          textAlign: 'center',
          fontSize: '0.8rem',
          color: 'var(--text-muted)',
          marginTop: 'auto',
        }}
      >
        <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', alignItems: 'center', gap: '1.5rem', maxWidth: '1240px', margin: '0 auto' }}>
          <span>© 2026 NirmaanSetu. All rights reserved.</span>
          <button
            onClick={() => setShowPrivacyPolicy(true)}
            style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer', fontSize: '0.8rem', textDecoration: 'underline' }}
          >
            Privacy Policy & Data Safety
          </button>
          <span>Support: +91 98765 43210</span>
          <span>v1.0.0 (Production)</span>
        </div>
      </footer>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <MainApp />
    </AuthProvider>
  );
};

export default App;
