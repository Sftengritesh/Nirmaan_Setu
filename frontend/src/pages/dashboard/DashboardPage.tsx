import React, { useState } from 'react';
import { CurrentUserResponse } from '../../services/authService';
import { MarketplacePage } from '../discovery/MarketplacePage';
import { WorkerProfilePage } from '../worker/WorkerProfilePage';
import { ContractorPage } from '../contractor/ContractorPage';
import { ClientPage } from '../client/ClientPage';
import { BookingPage } from '../booking/BookingPage';
import { VerificationPage } from '../verification/VerificationPage';
import { Search, UserCheck, Calendar, ShieldCheck, Briefcase, Building, HardHat } from 'lucide-react';

interface DashboardPageProps {
  currentUser: CurrentUserResponse;
  activeRole: string;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ currentUser, activeRole }) => {
  const [navTab, setNavTab] = useState<'MARKETPLACE' | 'ROLE_WORKSPACE' | 'BOOKINGS' | 'VERIFICATION'>('MARKETPLACE');

  return (
    <div>
      {/* Navigation Sub-Header */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          backgroundColor: 'var(--bg-surface)',
          padding: '0.5rem 1.5rem',
          borderBottom: '1px solid var(--border-color)',
          marginBottom: '1.5rem',
          borderRadius: 'var(--radius-md)',
        }}
      >
        <button
          className={`btn ${navTab === 'MARKETPLACE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setNavTab('MARKETPLACE')}
        >
          <Search size={16} /> Discovery Marketplace
        </button>

        <button
          className={`btn ${navTab === 'ROLE_WORKSPACE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setNavTab('ROLE_WORKSPACE')}
        >
          {activeRole === 'CLIENT' && <Building size={16} />}
          {activeRole === 'WORKER' && <HardHat size={16} />}
          {activeRole === 'CONTRACTOR' && <Briefcase size={16} />}
          {activeRole === 'ADMIN' && <ShieldCheck size={16} />}
          {activeRole} Workspace
        </button>

        {(activeRole === 'WORKER' || activeRole === 'CONTRACTOR') && (
          <button
            className={`btn ${navTab === 'BOOKINGS' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
            onClick={() => setNavTab('BOOKINGS')}
          >
            <Calendar size={16} /> Incoming Bookings
          </button>
        )}

        <button
          className={`btn ${navTab === 'VERIFICATION' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setNavTab('VERIFICATION')}
        >
          <ShieldCheck size={16} /> {activeRole === 'ADMIN' ? 'Admin Verifications' : 'Trust Verification'}
        </button>
      </div>

      {/* Render Workspace Content */}
      {navTab === 'MARKETPLACE' && <MarketplacePage />}

      {navTab === 'ROLE_WORKSPACE' && (
        <>
          {activeRole === 'WORKER' && <WorkerProfilePage />}
          {activeRole === 'CONTRACTOR' && <ContractorPage />}
          {activeRole === 'CLIENT' && <ClientPage />}
          {activeRole === 'ADMIN' && <VerificationPage activeRole="ADMIN" />}
        </>
      )}

      {navTab === 'BOOKINGS' && <BookingPage activeRole={activeRole} />}

      {navTab === 'VERIFICATION' && <VerificationPage activeRole={activeRole} />}
    </div>
  );
};
