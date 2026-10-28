import React, { useState, useEffect } from 'react';
import { Shield, Phone, KeyRound, HardHat, Building, Briefcase, ArrowRight, RefreshCw, CheckCircle2 } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { UserRole } from '../../types/schema';
import { validateIndianPhone } from '../../utils/validation';
import { checkRateLimit } from '../../utils/rateLimiter';
import { ErrorMessage } from '../../components/common/ErrorMessage';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';

export const AuthPage: React.FC = () => {
  const { startOtp, verifyOtp, onboard } = useAuth();

  // Multi-step flow: 'PHONE' -> 'OTP' -> 'ONBOARD'
  const [step, setStep] = useState<'PHONE' | 'OTP' | 'ONBOARD'>('PHONE');

  const [phone, setPhone] = useState('+919876543210');
  const [devOtp, setDevOtp] = useState<string | null>(null);
  const [otp, setOtp] = useState('');
  const [resendTimer, setResendTimer] = useState(0);

  // Onboarding state
  const [displayName, setDisplayName] = useState('');
  const [selectedRole, setSelectedRole] = useState<UserRole>('WORKER');

  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  // Countdown timer effect
  useEffect(() => {
    let interval: any;
    if (resendTimer > 0) {
      interval = setInterval(() => {
        setResendTimer((prev) => prev - 1);
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [resendTimer]);

  const handleSendOtp = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    setFormError(null);

    const rateCheck = checkRateLimit('AUTH_OTP_REQUEST', 5, 60000);
    if (!rateCheck.allowed) {
      setFormError(`Too many OTP requests. Please wait ${rateCheck.waitSeconds}s.`);
      return;
    }

    let cleanPhone = phone.trim();
    if (!cleanPhone.startsWith('+')) {
      cleanPhone = `+91${cleanPhone.replace(/^0+/, '')}`;
    }

    const phoneCheck = validateIndianPhone(cleanPhone);
    if (!phoneCheck.isValid) {
      setFormError(phoneCheck.error || 'Please enter a valid 10-digit Indian mobile number');
      return;
    }

    setLoading(true);
    try {
      const resp = await startOtp(cleanPhone);
      if (resp.devOtp) {
        setDevOtp(resp.devOtp);
        setOtp(resp.devOtp); // auto-fill in dev mode for smooth testing
      }
      setResendTimer(30);
      setStep('OTP');
    } catch (err: any) {
      setFormError(err.message || 'Failed to send OTP. Please check backend connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!otp || otp.length < 4) {
      setFormError('Please enter the verification code');
      return;
    }

    setLoading(true);
    try {
      let cleanPhone = phone.trim();
      if (!cleanPhone.startsWith('+')) {
        cleanPhone = `+91${cleanPhone.replace(/^0+/, '')}`;
      }
      const user = await verifyOtp(cleanPhone, otp.trim());
      if (!user.roles || user.roles.length === 0) {
        // Needs role onboarding
        setStep('ONBOARD');
      }
    } catch (err: any) {
      setFormError(err.message || 'Invalid or expired OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteOnboard = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!displayName.trim()) {
      setFormError('Please enter your name');
      return;
    }

    setLoading(true);
    try {
      await onboard(selectedRole, displayName.trim());
    } catch (err: any) {
      setFormError(err.message || 'Failed to complete profile onboarding');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '75vh',
        padding: '1rem',
      }}
    >
      <div
        className="card"
        style={{
          width: '100%',
          maxWidth: '440px',
          padding: '2.25rem',
          boxShadow: 'var(--shadow-xl)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-lg)',
          backgroundColor: 'var(--bg-surface)',
        }}
      >
        {/* Brand Banner */}
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: '56px',
              height: '56px',
              borderRadius: '16px',
              backgroundColor: 'var(--primary-color)',
              color: '#FFFFFF',
              marginBottom: '1rem',
              boxShadow: '0 8px 16px rgba(15, 32, 82, 0.25)',
            }}
          >
            <Shield size={30} />
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: 'var(--text-primary)', margin: '0 0 0.5rem 0' }}>
            NirmaanSetu
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', margin: 0 }}>
            Unified Construction Operations & Workforce Platform
          </p>
        </div>

        {formError && (
          <div style={{ marginBottom: '1.25rem' }}>
            <ErrorMessage message={formError} />
          </div>
        )}

        {/* STEP 1: PHONE NUMBER */}
        {step === 'PHONE' && (
          <form onSubmit={handleSendOtp} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div>
              <label
                style={{
                  display: 'block',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  color: 'var(--text-primary)',
                  marginBottom: '6px',
                }}
              >
                Mobile Number
              </label>
              <div style={{ position: 'relative' }}>
                <Phone
                  size={18}
                  style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'var(--text-muted)',
                  }}
                />
                <input
                  type="tel"
                  placeholder="+91 98765 43210"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 12px 10px 40px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-color)',
                    fontSize: '0.95rem',
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>
                Enter your registered mobile number with country code (+91)
              </span>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{
                width: '100%',
                padding: '11px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                fontSize: '0.95rem',
                fontWeight: 600,
              }}
            >
              {loading ? <LoadingSpinner size={18} /> : (
                <>
                  Get OTP Verification Code <ArrowRight size={16} />
                </>
              )}
            </button>
          </form>
        )}

        {/* STEP 2: OTP VERIFICATION */}
        {step === 'OTP' && (
          <form onSubmit={handleVerifyOtp} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            {devOtp && (
              <div
                style={{
                  padding: '10px 12px',
                  borderRadius: 'var(--radius-sm)',
                  backgroundColor: '#EFF6FF',
                  border: '1px solid #BFDBFE',
                  color: '#1D4ED8',
                  fontSize: '0.85rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <CheckCircle2 size={16} />
                <span>
                  <strong>Development Mode:</strong> OTP is <code>{devOtp}</code>
                </span>
              </div>
            )}

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                  Enter 6-digit Code
                </label>
                <button
                  type="button"
                  onClick={() => setStep('PHONE')}
                  style={{ background: 'none', border: 'none', color: 'var(--primary-color)', fontSize: '0.75rem', cursor: 'pointer' }}
                >
                  Change Number
                </button>
              </div>

              <div style={{ position: 'relative' }}>
                <KeyRound
                  size={18}
                  style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'var(--text-muted)',
                  }}
                />
                <input
                  type="text"
                  maxLength={6}
                  placeholder="123456"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '10px 12px 10px 40px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-color)',
                    fontSize: '1.1rem',
                    letterSpacing: '3px',
                    fontWeight: 700,
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Sent to {phone}</span>
              {resendTimer > 0 ? (
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Resend in {resendTimer}s</span>
              ) : (
                <button
                  type="button"
                  onClick={() => handleSendOtp()}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--primary-color)',
                    fontSize: '0.8rem',
                    fontWeight: 600,
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  <RefreshCw size={12} /> Resend OTP
                </button>
              )}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{
                width: '100%',
                padding: '11px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                fontSize: '0.95rem',
                fontWeight: 600,
              }}
            >
              {loading ? <LoadingSpinner size={18} /> : 'Verify & Continue'}
            </button>
          </form>
        )}

        {/* STEP 3: ROLE ONBOARDING (For fresh profiles) */}
        {step === 'ONBOARD' && (
          <form onSubmit={handleCompleteOnboard} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div style={{ textAlign: 'center', marginBottom: '0.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 700, margin: '0 0 4px 0' }}>Complete Your Profile</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
                Select your primary role to configure your workspace
              </p>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Full Name / Organization
              </label>
              <input
                type="text"
                placeholder="Ramesh Kumar / Apex Builders"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-color)',
                  boxSizing: 'border-box',
                }}
                required
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '6px' }}>
                Select Role
              </label>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px' }}>
                {[
                  { role: 'WORKER' as UserRole, icon: <HardHat size={20} />, title: 'Worker' },
                  { role: 'CLIENT' as UserRole, icon: <Building size={20} />, title: 'Client' },
                  { role: 'CONTRACTOR' as UserRole, icon: <Briefcase size={20} />, title: 'Contractor' },
                ].map((item) => (
                  <button
                    key={item.role}
                    type="button"
                    onClick={() => setSelectedRole(item.role)}
                    style={{
                      padding: '12px 8px',
                      borderRadius: 'var(--radius-sm)',
                      border: selectedRole === item.role ? '2px solid var(--primary-color)' : '1px solid var(--border-color)',
                      backgroundColor: selectedRole === item.role ? 'rgba(15, 32, 82, 0.06)' : 'var(--bg-primary)',
                      cursor: 'pointer',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      gap: '6px',
                    }}
                  >
                    <div style={{ color: selectedRole === item.role ? 'var(--primary-color)' : 'var(--text-muted)' }}>
                      {item.icon}
                    </div>
                    <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>{item.title}</span>
                  </button>
                ))}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{
                width: '100%',
                padding: '11px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '8px',
                fontSize: '0.95rem',
                fontWeight: 600,
              }}
            >
              {loading ? <LoadingSpinner size={18} /> : 'Complete Setup'}
            </button>
          </form>
        )}

        <div style={{ marginTop: '1.5rem', textAlign: 'center', borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
          <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: 0 }}>
            🔒 Secured by Spring Boot Backend OTP Engine & Token Persistence
          </p>
        </div>
      </div>
    </div>
  );
};
