import React, { createContext, useContext, useEffect, useState } from 'react';
import { UserProfile, UserRole } from '../types/schema';
import { apiClient, getAuthToken, setAuthToken } from '../services/apiClient';

export interface AuthUser {
  uid: string;
  phone: string;
  displayName: string;
  roles: UserRole[];
}

interface AuthContextType {
  currentUser: AuthUser | null;
  userProfile: UserProfile | null;
  activeRole: UserRole;
  loading: boolean;
  startOtp: (phone: string) => Promise<{ devMode: boolean; devOtp?: string }>;
  verifyOtp: (phone: string, otp: string) => Promise<AuthUser>;
  onboard: (role: UserRole, displayName?: string) => Promise<void>;
  logout: () => Promise<void>;
  setActiveRole: (role: UserRole) => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [activeRole, setActiveRole] = useState<UserRole>('ADMIN');
  const [loading, setLoading] = useState(true);

  const syncUserFromMe = (me: { userId: string; phone?: string; roles: string[] }) => {
    const roles = (me.roles || []).map((r) => r.toUpperCase() as UserRole);
    const initialRole: UserRole = roles.includes('ADMIN')
      ? 'ADMIN'
      : roles.includes('WORKER')
      ? 'WORKER'
      : roles.includes('CONTRACTOR')
      ? 'CONTRACTOR'
      : roles.includes('CLIENT')
      ? 'CLIENT'
      : 'WORKER';

    const phoneStr = me.phone || 'User';
    const authUser: AuthUser = {
      uid: me.userId,
      phone: phoneStr,
      displayName: phoneStr,
      roles: roles.length > 0 ? roles : ['WORKER'],
    };

    const profile: UserProfile = {
      uid: me.userId,
      email: `${phoneStr.replace(/[^0-9]/g, '')}@nirmaansetu.internal`,
      phoneNumber: phoneStr,
      displayName: phoneStr,
      role: initialRole,
      roles: roles,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    setCurrentUser(authUser);
    setUserProfile(profile);
    setActiveRole(initialRole);
    return authUser;
  };

  const refreshProfile = async () => {
    const token = getAuthToken();
    if (!token) {
      setCurrentUser(null);
      setUserProfile(null);
      return;
    }

    try {
      const me = await apiClient.auth.getMe();
      syncUserFromMe(me);
    } catch (err) {
      console.warn('Session expired or invalid, logging out', err);
      setAuthToken(null);
      setCurrentUser(null);
      setUserProfile(null);
    }
  };

  useEffect(() => {
    const initAuth = async () => {
      const token = getAuthToken();
      if (token) {
        try {
          const me = await apiClient.auth.getMe();
          syncUserFromMe(me);
        } catch (err) {
          console.warn('Failed to restore session:', err);
          setAuthToken(null);
          setCurrentUser(null);
          setUserProfile(null);
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const startOtp = async (phone: string) => {
    return await apiClient.auth.startOtp(phone);
  };

  const verifyOtp = async (phone: string, otp: string): Promise<AuthUser> => {
    setLoading(true);
    try {
      const resp = await apiClient.auth.verifyOtp(phone, otp);
      setAuthToken(resp.accessToken);
      const user = syncUserFromMe({
        userId: resp.userId,
        phone: phone,
        roles: resp.roles || [],
      });
      return user;
    } finally {
      setLoading(false);
    }
  };

  const onboard = async (role: UserRole, displayName?: string) => {
    setLoading(true);
    try {
      const updated = await apiClient.auth.onboard(role, displayName);
      syncUserFromMe(updated);
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setAuthToken(null);
    setCurrentUser(null);
    setUserProfile(null);
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        userProfile,
        activeRole,
        loading,
        startOtp,
        verifyOtp,
        onboard,
        logout,
        setActiveRole,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
