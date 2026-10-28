import { Capacitor } from '@capacitor/core';
import { Geolocation } from '@capacitor/geolocation';
import { GPSCoordinates } from '../types/schema';

/**
 * Calculates the great-circle distance between two GPS points using the Haversine formula
 * Returns distance in meters
 */
export const calculateDistanceMeters = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const R = 6371e3; // Earth radius in meters
  const phi1 = (lat1 * Math.PI) / 180;
  const phi2 = (lat2 * Math.PI) / 180;
  const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
  const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return Math.round(R * c);
};

/**
 * Validates if worker location is inside the site's geofence radius
 */
export const validateSiteGeofence = (
  workerCoords: GPSCoordinates,
  siteLat: number,
  siteLng: number,
  radiusMeters: number
): { isWithinGeofence: boolean; distanceMeters: number } => {
  const distance = calculateDistanceMeters(
    workerCoords.latitude,
    workerCoords.longitude,
    siteLat,
    siteLng
  );
  return {
    isWithinGeofence: distance <= radiusMeters,
    distanceMeters: distance,
  };
};

/**
 * Platform-Aware Geolocation Helper
 * Uses @capacitor/geolocation on native Android/iOS devices for fast, hardware GPS locking.
 * Falls back to HTML5 navigator.geolocation in standard desktop/mobile browsers.
 */
export const getCurrentGpsLocation = async (): Promise<GPSCoordinates> => {
  if (Capacitor.isNativePlatform()) {
    try {
      // Check and request Android runtime permissions
      const permStatus = await Geolocation.checkPermissions();
      if (permStatus.location !== 'granted') {
        const req = await Geolocation.requestPermissions();
        if (req.location !== 'granted') {
          throw new Error('Location permission denied on device. Please enable GPS permissions in App Settings.');
        }
      }

      const position = await Geolocation.getCurrentPosition({
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 5000,
      });

      return {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy ? Math.round(position.coords.accuracy) : undefined,
      };
    } catch (err: any) {
      console.warn('Native GPS acquisition error, falling back to browser geolocation:', err);
      // Fall through to browser geolocation if native fails
    }
  }

  // Standard Web Browser Geolocation
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation is not supported by your browser or device'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: Math.round(position.coords.accuracy),
        });
      },
      (error) => {
        let msg = 'Unable to retrieve location';
        if (error.code === error.PERMISSION_DENIED) {
          msg = 'Location permission was denied. Please allow GPS access in your browser.';
        } else if (error.code === error.POSITION_UNAVAILABLE) {
          msg = 'Location information is unavailable. Ensure device location is turned ON.';
        } else if (error.code === error.TIMEOUT) {
          msg = 'Location request timed out. Please retry.';
        }
        reject(new Error(msg));
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 0,
      }
    );
  });
};
