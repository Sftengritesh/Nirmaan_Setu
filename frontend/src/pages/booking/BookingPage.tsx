import React, { useState, useEffect } from 'react';
import { bookingService, BookingResponse } from '../../services/bookingService';
import { Check, X, Calendar, Clock, AlertCircle } from 'lucide-react';

interface BookingPageProps {
  activeRole: string;
}

export const BookingPage: React.FC<BookingPageProps> = ({ activeRole }) => {
  const [providerBookings, setProviderBookings] = useState<BookingResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const loadProviderBookings = async () => {
    setLoading(true);
    try {
      const data = await bookingService.getProviderBookings();
      setProviderBookings(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProviderBookings();
  }, [activeRole]);

  const handleAccept = async (bookingId: string) => {
    setError(null);
    try {
      await bookingService.acceptBooking(bookingId);
      setSuccess('Booking request accepted successfully!');
      loadProviderBookings();
    } catch (err: any) {
      setError(err.message || 'Failed to accept booking');
    }
  };

  const handleReject = async (bookingId: string) => {
    setError(null);
    try {
      await bookingService.rejectBooking(bookingId);
      setSuccess('Booking request rejected.');
      loadProviderBookings();
    } catch (err: any) {
      setError(err.message || 'Failed to reject booking');
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading bookings...</div>;

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Workforce Booking Requests</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          Incoming booking proposals from clients for your individual profile or managed teams
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '1.25rem' }}>
        {providerBookings.length === 0 ? (
          <div className="card" style={{ gridColumn: '1 / -1', padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No incoming booking requests found.
          </div>
        ) : (
          providerBookings.map((b) => (
            <div key={b.id} className="card">
              <div className="card-header">
                <span className={`badge badge-${b.status.toLowerCase()}`}>{b.status}</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Qty: {b.quantity}</span>
              </div>

              <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>
                Booking Proposal ({b.providerType})
              </h4>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>
                Requirement ID: <code style={{ color: 'var(--primary-color)' }}>{b.requirementId}</code>
              </p>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '1rem' }}>
                Requested at {new Date(b.requestedAt).toLocaleString()}
              </span>

              {b.status === 'REQUESTED' && (
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    onClick={() => handleAccept(b.id)}
                    className="btn btn-primary btn-sm"
                    style={{ flex: 1 }}
                  >
                    <Check size={16} /> Accept Proposal
                  </button>
                  <button
                    onClick={() => handleReject(b.id)}
                    className="btn btn-danger btn-sm"
                    style={{ flex: 1 }}
                  >
                    <X size={16} /> Reject
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};
