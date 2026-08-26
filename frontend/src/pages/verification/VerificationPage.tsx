import React, { useState, useEffect } from 'react';
import { verificationService, VerificationResponse } from '../../services/verificationService';
import { ShieldCheck, CheckCircle, XCircle, Clock, Send, ShieldAlert } from 'lucide-react';

interface VerificationPageProps {
  activeRole: string;
}

export const VerificationPage: React.FC<VerificationPageProps> = ({ activeRole }) => {
  const [myVerifications, setMyVerifications] = useState<VerificationResponse[]>([]);
  const [adminVerifications, setAdminVerifications] = useState<VerificationResponse[]>([]);
  const [filterSubject, setFilterSubject] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Admin Review State
  const [selectedVerificationId, setSelectedVerificationId] = useState<string | null>(null);
  const [reviewNotes, setReviewNotes] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      if (activeRole === 'ADMIN') {
        const adminData = await verificationService.listAdminVerifications(filterSubject, filterStatus);
        setAdminVerifications(adminData);
      } else {
        const myData = await verificationService.getMyVerifications();
        setMyVerifications(myData);
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [activeRole, filterSubject, filterStatus]);

  const handleSubmitUserVerification = async () => {
    setError(null);
    try {
      await verificationService.submitUserVerification();
      setSuccess('User verification request submitted.');
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to submit verification');
    }
  };

  const handleSubmitRoleVerification = async () => {
    setError(null);
    try {
      if (activeRole === 'WORKER') await verificationService.submitWorkerVerification();
      else if (activeRole === 'CONTRACTOR') await verificationService.submitContractorVerification();
      else if (activeRole === 'CLIENT') await verificationService.submitClientVerification();
      setSuccess(`${activeRole} verification request submitted.`);
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to submit verification');
    }
  };

  const handleAdminApprove = async (id: string) => {
    setError(null);
    try {
      await verificationService.verifyVerification(id, reviewNotes);
      setSuccess('Verification approved successfully.');
      setReviewNotes('');
      setSelectedVerificationId(null);
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to approve verification');
    }
  };

  const handleAdminReject = async (id: string) => {
    setError(null);
    try {
      await verificationService.rejectVerification(id, reviewNotes);
      setSuccess('Verification rejected.');
      setReviewNotes('');
      setSelectedVerificationId(null);
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to reject verification');
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading verification dashboard...</div>;

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>
          {activeRole === 'ADMIN' ? 'Admin Identity Verification Review Workspace' : 'Identity & Profile Verification'}
        </h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          {activeRole === 'ADMIN'
            ? 'Inspect and review manual identity verification requests across platform users'
            : 'Submit verification requests to acquire verified trust badge'}
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {activeRole !== 'ADMIN' ? (
        <div style={{ maxWidth: '800px' }}>
          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <h4 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>Submit Verification Request</h4>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button onClick={handleSubmitUserVerification} className="btn btn-secondary">
                <ShieldCheck size={16} /> Submit Account Verification
              </button>
              <button onClick={handleSubmitRoleVerification} className="btn btn-primary">
                <Send size={16} /> Submit {activeRole} Profile Verification
              </button>
            </div>
          </div>

          <h4 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>My Verification Submissions</h4>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {myVerifications.length === 0 ? (
              <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No verification requests submitted yet.
              </div>
            ) : (
              myVerifications.map((v) => (
                <div key={v.id} className="card" style={{ padding: '1rem' }}>
                  <div className="card-header" style={{ marginBottom: '0.25rem' }}>
                    <span style={{ fontWeight: 600 }}>{v.subjectType} Verification</span>
                    <span className={`badge badge-${v.status.toLowerCase()}`}>{v.status}</span>
                  </div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    Submitted {new Date(v.createdAt).toLocaleDateString()}
                  </span>
                  {v.notes && (
                    <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                      Review Notes: {v.notes}
                    </p>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      ) : (
        /* Admin Review Workspace */
        <div>
          <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <select value={filterSubject} onChange={(e) => setFilterSubject(e.target.value)} style={{ flex: 1 }}>
                <option value="">All Subject Types</option>
                <option value="USER">USER</option>
                <option value="WORKER">WORKER</option>
                <option value="CONTRACTOR">CONTRACTOR</option>
                <option value="CLIENT">CLIENT</option>
              </select>
              <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)} style={{ flex: 1 }}>
                <option value="">All Statuses</option>
                <option value="PENDING">PENDING</option>
                <option value="VERIFIED">VERIFIED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.25rem' }}>
            {adminVerifications.length === 0 ? (
              <div className="card" style={{ gridColumn: '1 / -1', padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No verification requests matching criteria.
              </div>
            ) : (
              adminVerifications.map((v) => (
                <div key={v.id} className="card">
                  <div className="card-header">
                    <span className="badge badge-admin">{v.subjectType}</span>
                    <span className={`badge badge-${v.status.toLowerCase()}`}>{v.status}</span>
                  </div>

                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>
                    User ID: <code style={{ color: 'var(--primary-color)' }}>{v.subjectUserId}</code>
                  </p>

                  {v.status === 'PENDING' && (
                    <div style={{ marginTop: '1rem' }}>
                      <textarea
                        rows={2}
                        value={selectedVerificationId === v.id ? reviewNotes : ''}
                        onChange={(e) => {
                          setSelectedVerificationId(v.id);
                          setReviewNotes(e.target.value);
                        }}
                        placeholder="Optional review notes..."
                        style={{ marginBottom: '0.5rem', fontSize: '0.8125rem' }}
                      />
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button
                          onClick={() => handleAdminApprove(v.id)}
                          className="btn btn-primary btn-sm"
                          style={{ flex: 1 }}
                        >
                          <CheckCircle size={14} /> Approve
                        </button>
                        <button
                          onClick={() => handleAdminReject(v.id)}
                          className="btn btn-danger btn-sm"
                          style={{ flex: 1 }}
                        >
                          <XCircle size={14} /> Reject
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
