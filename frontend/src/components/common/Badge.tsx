import React from 'react';

interface BadgeProps {
  label: string;
  variant?: 'success' | 'warning' | 'error' | 'info' | 'neutral';
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({ label, variant = 'neutral', size = 'sm' }) => {
  const styles: Record<string, { bg: string; color: string; border: string }> = {
    success: { bg: 'rgba(22, 163, 74, 0.1)', color: '#15803D', border: 'rgba(22, 163, 74, 0.2)' },
    warning: { bg: 'rgba(217, 119, 6, 0.1)', color: '#B45309', border: 'rgba(217, 119, 6, 0.2)' },
    error: { bg: 'rgba(186, 26, 26, 0.1)', color: '#BA1A1A', border: 'rgba(186, 26, 26, 0.2)' },
    info: { bg: 'rgba(15, 32, 82, 0.08)', color: '#0F2052', border: 'rgba(15, 32, 82, 0.2)' },
    neutral: { bg: 'rgba(117, 118, 128, 0.1)', color: '#5F6F8F', border: 'rgba(117, 118, 128, 0.2)' },
  };

  const style = styles[variant] || styles.neutral;
  const isSm = size === 'sm';

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: isSm ? '2px 8px' : '4px 12px',
        borderRadius: '9999px',
        fontSize: isSm ? '0.75rem' : '0.875rem',
        fontWeight: 600,
        backgroundColor: style.bg,
        color: style.color,
        border: `1px solid ${style.border}`,
        lineHeight: 1.25,
      }}
    >
      {label}
    </span>
  );
};
