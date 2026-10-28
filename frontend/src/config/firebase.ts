import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore, initializeFirestore, persistentLocalCache, persistentMultipleTabManager } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';
import { getMessaging, isSupported } from 'firebase/messaging';

// Production Firebase Configuration sourcing from Vite environment variables
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || 'AIzaSyA_Production_NirmaanSetu_Key',
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || 'nirmaansetu-prod.firebaseapp.com',
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || 'nirmaansetu-prod',
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || 'nirmaansetu-prod.appspot.com',
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '782910482910',
  appId: import.meta.env.VITE_FIREBASE_APP_ID || '1:782910482910:web:9f8d7c6b5a4e3d2c1',
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || 'G-NIRMAANSETU'
};

// Initialize App singleton
export const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();

// Initialize Firebase Authentication
export const auth = getAuth(app);

// Initialize Firestore with offline multi-tab persistence
let firestoreInstance;
try {
  firestoreInstance = initializeFirestore(app, {
    localCache: persistentLocalCache({
      tabManager: persistentMultipleTabManager()
    })
  });
} catch (e) {
  firestoreInstance = getFirestore(app);
}
export const db = firestoreInstance;

// Initialize Firebase Storage
export const storage = getStorage(app);

// Initialize Firebase Cloud Messaging safely (handles SSR / unsupported browsers)
export const getFcmMessaging = async () => {
  const supported = await isSupported();
  if (supported) {
    return getMessaging(app);
  }
  return null;
};
