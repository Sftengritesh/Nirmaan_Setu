# NirmaanSetu Frontend Architecture Specification

## 1. Executive Summary

This document specifies the frontend architecture guidelines, authentication flows, error handling patterns, state management strategies, and API client conventions required to build web and mobile interfaces for NirmaanSetu.

---

## 2. Authentication & Session Strategy

### Token Handling & Storage
- Authentication uses bearer token sessions issued by `POST /api/auth/otp/verify`.
- The `sessionToken` returned must be passed in the `Authorization` header as:
  ```text
  Authorization: Bearer <sessionToken>
  ```
- **Storage Policy**:
  - Web: Store token in secure `HttpOnly` cookie or memory state (e.g. React Context / Zustand) with fallback to `sessionStorage`.
  - Mobile (React Native / Flutter): Store in OS Secure Keychain / EncryptedSharedPreferences.

### Multi-Role Identity Architecture
- Users in NirmaanSetu do **NOT** have a single static role.
- Server returns an array of roles in `/api/auth/me`: `["CLIENT", "WORKER", "CONTRACTOR", "ADMIN"]`.
- The frontend UI must dynamically render role-specific navigation tabs, actions, and features based on the roles present in the `roles` array.

---

## 3. Global Error Handling Strategy

All backend exception handlers return JSON objects. The frontend API client must implement a global interceptor:

```typescript
export interface ApiError {
  code: string;
  message: string;
}

// Global Axios / Fetch Interceptor Logic
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Session expired or unauthenticated -> redirect to /login
      clearAuthSession();
    }
    return Promise.reject(error.response?.data || { code: 'UNKNOWN_ERROR', message: 'An unexpected error occurred.' });
  }
);
```

---

## 4. State Management & Polling Strategy

- **Unread Notifications**: Poll `GET /api/notifications/unread-count` every 30 to 60 seconds when user is active.
- **Form Validations**: Perform client-side validation prior to API submission matching backend field constraints (e.g., non-blank phone numbers, positive daily rates).
- **Pagination**: Handle Spring Data `Page<T>` response structures (`content`, `pageable`, `totalElements`, `totalPages`, `number`, `size`).
