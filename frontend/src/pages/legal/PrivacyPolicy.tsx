import React from 'react';
import { Shield, Lock, MapPin, Camera, Database, Trash2, ArrowLeft } from 'lucide-react';

interface PrivacyPolicyProps {
  onBack?: () => void;
}

export const PrivacyPolicy: React.FC<PrivacyPolicyProps> = ({ onBack }) => {
  return (
    <div style={{ maxWidth: '840px', margin: '0 auto', padding: '2rem 1rem', color: 'var(--text-primary)' }}>
      {onBack && (
        <button
          onClick={onBack}
          className="btn btn-secondary btn-sm"
          style={{ marginBottom: '1.5rem', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
        >
          <ArrowLeft size={16} /> Back to Dashboard
        </button>
      )}

      <div className="card" style={{ padding: '2.5rem', backgroundColor: 'var(--bg-surface)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '1.5rem' }}>
          <div
            style={{
              padding: '10px',
              borderRadius: '12px',
              backgroundColor: 'var(--primary-light)',
              color: 'var(--primary-color)',
            }}
          >
            <Shield size={28} />
          </div>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, margin: 0 }}>NirmaanSetu Privacy Policy</h1>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
              Effective Date: October 28, 2026 | Version 1.0 (Google Play Compliant)
            </span>
          </div>
        </div>

        <p style={{ lineHeight: 1.6, color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
          NirmaanSetu ("we", "our", or "the Platform") is a construction workforce management and site operations system.
          We are committed to transparent and secure processing of construction business data, field attendance, and user identity.
        </p>

        <hr style={{ border: 'none', borderTop: '1px solid var(--border-color)', margin: '1.5rem 0' }} />

        {/* Section 1: Permissions */}
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <MapPin size={20} color="var(--primary-color)" /> 1. Location Data & Geofence Verification
          </h2>
          <p style={{ lineHeight: 1.6, color: 'var(--text-secondary)' }}>
            NirmaanSetu collects device location (latitude and longitude) <strong>only when a worker explicitly initiates a Check-In or Check-Out</strong>.
            This location is used solely to verify proximity to the authorized construction site boundary (geofence radius) and prevent proxy attendance.
            We <strong>do not</strong> track your location continuously in the background when the app is closed or not in use.
          </p>
        </div>

        {/* Section 2: Camera */}
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Camera size={20} color="var(--primary-color)" /> 2. Camera & Photo Capture
          </h2>
          <p style={{ lineHeight: 1.6, color: 'var(--text-secondary)' }}>
            Camera access is requested during daily attendance check-in to capture a live verification selfie.
            Contractors and supervisors also take construction progress photographs for Daily Progress Reports (DPR).
            All images are stored in secure, durable cloud storage and are only accessible by authorized project managers.
          </p>
        </div>

        {/* Section 3: Data Safety */}
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Lock size={20} color="var(--primary-color)" /> 3. Data Protection & Security
          </h2>
          <p style={{ lineHeight: 1.6, color: 'var(--text-secondary)' }}>
            All communications between the mobile application, website, and Spring Boot backend are encrypted using TLS 1.3 (HTTPS).
            Authentication is secured via one-time passwords (OTP) and signed HMAC session tokens.
            We never sell, rent, or monetize personal information or operational construction records to third parties.
          </p>
        </div>

        {/* Section 4: Data Retention & Account Deletion */}
        <div style={{ marginBottom: '2rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Trash2 size={20} color="#DC2626" /> 4. Data Retention & Account Deletion
          </h2>
          <p style={{ lineHeight: 1.6, color: 'var(--text-secondary)' }}>
            In compliance with Google Play Data Safety policies, users have the right to request deletion of their account and associated personal data.
            To request account deletion:
          </p>
          <ul style={{ lineHeight: 1.6, color: 'var(--text-secondary)', paddingLeft: '1.25rem' }}>
            <li>Contact our Data Privacy team at <strong>privacy@nirmaansetu.in</strong> or <strong>support@nirmaansetu.in</strong> with your registered phone number.</li>
            <li>Alternatively, send a written request via our Admin Support Helpline: <strong>+91 98765 43210</strong>.</li>
            <li>Upon verification, your profile, active sessions, and direct identifiers will be purged within 30 days, subject to statutory labor ledger retention rules.</li>
          </ul>
        </div>

        <div style={{ backgroundColor: 'var(--bg-container)', padding: '1rem', borderRadius: 'var(--radius-sm)', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
          For inquiries or grievance redressal, contact: Compliance Officer, NirmaanSetu Technologies, Sector 62, Noida, UP 201309, India.
        </div>
      </div>
    </div>
  );
};
