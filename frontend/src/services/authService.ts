import { request, setStoredToken, removeStoredToken } from './apiClient';

export interface AuthResponse {
  sessionToken: string;
  userId: string;
  phoneNumber: string;
  roles: string[];
}

export interface CurrentUserResponse {
  userId: string;
  phoneNumber: string;
  roles: string[];
}

export const authService = {
  async startOtp(phoneNumber: string): Promise<void> {
    await request<void>('/api/auth/otp/start', {
      method: 'POST',
      body: JSON.stringify({ phoneNumber }),
    });
  },

  async verifyOtp(phoneNumber: string, otp: string): Promise<AuthResponse> {
    const res = await request<AuthResponse>('/api/auth/otp/verify', {
      method: 'POST',
      body: JSON.stringify({ phoneNumber, otp }),
    });
    if (res.sessionToken) {
      setStoredToken(res.sessionToken);
    }
    return res;
  },

  async getMe(): Promise<CurrentUserResponse> {
    return request<CurrentUserResponse>('/api/auth/me');
  },

  async logout(): Promise<void> {
    try {
      await request<void>('/api/auth/logout', { method: 'POST' });
    } finally {
      removeStoredToken();
    }
  },
};
