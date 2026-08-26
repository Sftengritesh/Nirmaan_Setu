import { request } from './apiClient';

export interface ClientProfileResponse {
  id: string;
  userId: string;
  clientType: string;
  displayName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateClientProfileRequest {
  clientType: string;
  displayName: string;
}

export const clientService = {
  async createProfile(req: CreateClientProfileRequest): Promise<ClientProfileResponse> {
    return request<ClientProfileResponse>('/api/clients/profile', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getProfile(): Promise<ClientProfileResponse> {
    return request<ClientProfileResponse>('/api/clients/profile/me');
  },

  async updateProfile(req: CreateClientProfileRequest): Promise<ClientProfileResponse> {
    return request<ClientProfileResponse>('/api/clients/profile/me', {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },
};
