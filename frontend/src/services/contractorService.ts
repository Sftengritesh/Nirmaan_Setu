import { request } from './apiClient';

export interface ContractorProfileResponse {
  id: string;
  userId: string;
  displayName: string;
  description?: string;
  location: string;
  createdAt: string;
  updatedAt: string;
}

export interface ContractorWorkerResponse {
  contractorProfileId: string;
  workerProfileId: string;
  startsOn: string;
  endsOn?: string;
}

export interface CreateContractorProfileRequest {
  displayName: string;
  location: string;
  description?: string;
}

export interface AssociateWorkerRequest {
  workerProfileId: string;
  startsOn: string;
  endsOn?: string;
}

export const contractorService = {
  async createProfile(req: CreateContractorProfileRequest): Promise<ContractorProfileResponse> {
    return request<ContractorProfileResponse>('/api/contractors/profile', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getProfile(): Promise<ContractorProfileResponse> {
    return request<ContractorProfileResponse>('/api/contractors/profile/me');
  },

  async updateProfile(req: CreateContractorProfileRequest): Promise<ContractorProfileResponse> {
    return request<ContractorProfileResponse>('/api/contractors/profile/me', {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },

  async associateWorker(req: AssociateWorkerRequest): Promise<ContractorWorkerResponse> {
    return request<ContractorWorkerResponse>('/api/contractors/workers', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getAssociatedWorkers(): Promise<ContractorWorkerResponse[]> {
    return request<ContractorWorkerResponse[]>('/api/contractors/workers');
  },

  async endWorkerAssociation(workerProfileId: string, startsOn: string, endsOn?: string): Promise<ContractorWorkerResponse> {
    const params = new URLSearchParams({ startsOn });
    if (endsOn) params.append('endsOn', endsOn);
    return request<ContractorWorkerResponse>(`/api/contractors/workers/${workerProfileId}/end?${params.toString()}`, {
      method: 'POST',
    });
  },
};
