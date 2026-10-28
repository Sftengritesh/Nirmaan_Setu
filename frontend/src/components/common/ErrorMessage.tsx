import React from 'react';
import { AlertCircle, RefreshCw } from 'lucide-react';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onRetry }) => {
  return (
    <div
      className="alert alert-error"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0.875rem 1.25rem',
        borderRadius: 'var(--radius-md)',
        marginBottom: '1rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
        <AlertCircle size={20} style={{ flexShrink: 0 }} />
        <span style={{ fontSize: '0.875rem' }}>{message}</span>
      </div>
      {onRetry && (
        <button
          className="btn btn-secondary btn-sm"
          style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', height: 'auto' }}
          onClick={onRetry}
        >
          <RefreshCw size={12} /> Retry
        </button>
      )}
    </div>
  );
};
