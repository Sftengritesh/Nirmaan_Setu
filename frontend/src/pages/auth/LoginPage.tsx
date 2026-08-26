import React, { useState } from 'react';
import { authService, AuthResponse } from '../../services/authService';
import { Shield, Phone, KeyRound, ArrowRight } from 'lucide-react';

interface LoginPageProps {
  onLoginSuccess: (authData: AuthResponse) => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [phoneNumber, setPhoneNumber] = useState('+919876543210');
  const [otp, setOtp] = useState('123456');
  const [step, setStep] = useState<'PHONE' | 'OTP'>('PHONE');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleStartOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await authService.startOtp(phoneNumber);
      setStep('OTP');
    } catch (err: any) {
      setError(err.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const authData = await authService.verifyOtp(phoneNumber, otp);
      onLoginSuccess(authData);
    } catch (err: any) {
      setError(err.message || 'Invalid OTP code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
      <div className="card" style={{ maxWidth: '440px', width: '100%', padding: '2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{ display: 'inline-flex', padding: '0.75rem', borderRadius: '50%', backgroundColor: 'var(--primary-light)', color: 'var(--primary-color)', marginBottom: '0.75rem' }}>
            <Shield size={28} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>NirmaanSetu Access</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            Workforce & Construction Marketplace Portal
          </p>
        </div>

        {error && (
          <div className="alert alert-error">
            <span>{error}</span>
          </div>
        )}

        {step === 'PHONE' ? (
          <form onSubmit={handleStartOtp}>
            <div style={{ marginBottom: '1.25rem' }}>
              <label>Phone Number</label>
              <div style={{ position: 'relative' }}>
                <Phone size={18} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  type="text"
                  value={phoneNumber}
                  onChange={(e) => setPhoneNumber(e.target.value)}
                  placeholder="+91..."
                  style={{ paddingLeft: '2.25rem' }}
                  required
                />
              </div>
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Sending OTP...' : 'Continue'} <ArrowRight size={16} />
            </button>
          </form>
        ) : (
          <form onSubmit={handleVerifyOtp}>
            <div style={{ marginBottom: '1.25rem' }}>
              <label>Enter 6-Digit OTP</label>
              <div style={{ position: 'relative' }}>
                <KeyRound size={18} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  placeholder="123456"
                  style={{ paddingLeft: '2.25rem', letterSpacing: '0.25em', fontWeight: 'bold' }}
                  required
                />
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.375rem', display: 'block' }}>
                Sent to {phoneNumber}
              </span>
            </div>
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
              {loading ? 'Verifying...' : 'Verify & Sign In'}
            </button>
            <button
              type="button"
              onClick={() => setStep('PHONE')}
              className="btn btn-secondary"
              style={{ width: '100%', marginTop: '0.5rem' }}
            >
              Change Phone Number
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
