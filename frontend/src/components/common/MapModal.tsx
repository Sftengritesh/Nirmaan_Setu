import React from 'react';
import { MapPin, Navigation, Compass, ExternalLink } from 'lucide-react';
import { Modal } from './Modal';
import { SiteRecord, GPSCoordinates } from '../../types/schema';

interface MapModalProps {
  isOpen: boolean;
  onClose: () => void;
  site: SiteRecord;
  workerLocation?: GPSCoordinates;
  distanceMeters?: number;
  isWithinGeofence?: boolean;
}

export const MapModal: React.FC<MapModalProps> = ({
  isOpen,
  onClose,
  site,
  workerLocation,
  distanceMeters,
  isWithinGeofence
}) => {
  const googleMapsUrl = `https://www.google.com/maps/search/?api=1&query=${site.latitude},${site.longitude}`;
  const embedUrl = `https://maps.google.com/maps?q=${site.latitude},${site.longitude}&hl=en&z=16&output=embed`;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Site Geofence Tracker: ${site.name}`} maxWidth="680px">
      <div>
        {/* Site Details Banner */}
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0.75rem 1rem',
            backgroundColor: 'var(--bg-surface-hover)',
            borderRadius: 'var(--radius-sm)',
            marginBottom: '1rem',
            gap: '0.5rem'
          }}
        >
          <div>
            <div style={{ fontWeight: 600, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <MapPin size={16} color="var(--primary-color)" /> {site.name}
            </div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              {site.address}, {site.city}, {site.state}
            </div>
          </div>

          <a
            href={googleMapsUrl}
            target="_blank"
            rel="noreferrer"
            className="btn btn-secondary btn-sm"
            style={{ textDecoration: 'none' }}
          >
            <ExternalLink size={14} /> Open in Google Maps
          </a>
        </div>

        {/* Status Pills */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '0.5rem', marginBottom: '1rem' }}>
          <div style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Site Coordinates</span>
            <p style={{ margin: 0, fontWeight: 600, fontSize: '0.8rem' }}>{site.latitude.toFixed(5)}, {site.longitude.toFixed(5)}</p>
          </div>

          <div style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Geofence Radius</span>
            <p style={{ margin: 0, fontWeight: 600, fontSize: '0.8rem' }}>{site.geofenceRadiusMeters} meters</p>
          </div>

          {distanceMeters !== undefined && (
            <div style={{ padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', textAlign: 'center' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Distance from Center</span>
              <p style={{ margin: 0, fontWeight: 600, fontSize: '0.8rem', color: isWithinGeofence ? 'var(--success-color)' : 'var(--error-color)' }}>
                {distanceMeters}m ({isWithinGeofence ? 'Inside' : 'Outside'})
              </p>
            </div>
          )}
        </div>

        {/* Embedded Map Frame */}
        <div
          style={{
            width: '100%',
            height: '350px',
            borderRadius: 'var(--radius-md)',
            overflow: 'hidden',
            border: '1px solid var(--border-color)',
            position: 'relative'
          }}
        >
          <iframe
            title="Google Maps Site Tracking"
            width="100%"
            height="100%"
            frameBorder="0"
            scrolling="no"
            src={embedUrl}
            style={{ border: 0 }}
          />

          {/* Overlay Geofence indicator */}
          <div
            style={{
              position: 'absolute',
              bottom: '12px',
              left: '12px',
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              padding: '6px 12px',
              borderRadius: '9999px',
              boxShadow: '0 2px 6px rgba(0,0,0,0.15)',
              fontSize: '0.75rem',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <Compass size={14} color="var(--primary-color)" />
            <span>Target Site Geofence Zone (r = {site.geofenceRadiusMeters}m)</span>
          </div>
        </div>

        {workerLocation && (
          <div style={{ marginTop: '0.75rem', fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Navigation size={14} color="var(--success-color)" />
            <span>Worker GPS Recorded at: Lat {workerLocation.latitude.toFixed(5)}, Lng {workerLocation.longitude.toFixed(5)} (Accuracy: ±{workerLocation.accuracy || 10}m)</span>
          </div>
        )}
      </div>
    </Modal>
  );
};
