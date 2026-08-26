import { request } from './apiClient';

export interface VerificationResponse {
  id: string;
  subjectType: string;
  subjectUserId: string;
  subjectWorkerProfileId?: string;
  subjectContractorProfileId?: string;
  subjectClientProfileId?: string;
  verificationType: string;
  status: string;
  reviewedByUserId?: string;
  reviewedAt?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewVerificationRequest {
  notes?: string;
}

export const verificationService = {
  async submitUserVerification(): Promise<VerificationResponse> {
    return request<VerificationResponse>('/api/verifications/user', { method: 'POST' });
  },

  async submitWorkerVerification(): Promise<VerificationResponse> {
    return request<VerificationResponse>('/api/verifications/worker', { method: 'POST' });
  },

  async submitContractorVerification(): Promise<VerificationResponse> {
    return request<VerificationResponse>('/api/verifications/contractor', { method: 'POST' });
  },

  async submitClientVerification(): Promise<VerificationResponse> {
    return request<VerificationResponse>('/api/verifications/client', { method: 'POST' });
  },

  async getMyVerifications(): Promise<VerificationResponse[]> {
    return request<VerificationResponse[]>('/api/verifications/me');
  },

  async listAdminVerifications(subjectType?: string, status?: string): Promise<VerificationResponse[]> {
    const params = new URLSearchParams();
    if (subjectType) params.append('subjectType', subjectType);
    if (status) params.append('status', status);
    return request<VerificationResponse[]>(`/api/admin/verifications?${params.toString()}`);
  },

  async getAdminVerificationById(id: string): Promise<VerificationResponse> {
    return request<VerificationResponse>(`/api/admin/verifications/${id}`);
  },

  async verifyVerification(id: string, notes?: string): Promise<VerificationResponse> {
    return request<VerificationResponse>(`/api/admin/verifications/${id}/verify`, {
      method: 'POST',
      body: JSON.stringify({ notes }),
    });
  },

  async rejectVerification(id: string, notes?: string): Promise<VerificationResponse> {
    return request<VerificationResponse>(`/api/admin/verifications/${id}/reject`, {
      method: 'POST',
      body: JSON.stringify({ notes }),
    });
  },
};
