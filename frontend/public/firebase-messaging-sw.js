// Firebase Cloud Messaging Background Service Worker
importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.8.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyA_Production_NirmaanSetu_Key",
  authDomain: "nirmaansetu-prod.firebaseapp.com",
  projectId: "nirmaansetu-prod",
  storageBucket: "nirmaansetu-prod.appspot.com",
  messagingSenderId: "782910482910",
  appId: "1:782910482910:web:9f8d7c6b5a4e3d2c1"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message: ', payload);
  const notificationTitle = payload.notification?.title || 'NirmaanSetu Update';
  const notificationOptions = {
    body: payload.notification?.body || 'New site notification received.',
    icon: '/icon-192.png',
    badge: '/favicon.ico',
    data: payload.data
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});
