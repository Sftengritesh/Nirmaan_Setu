import { request } from './apiClient';

export interface SkillResponse {
  id: string;
  code: string;
  name: string;
}

export interface WorkerProfileResponse {
  id: string;
  userId: string;
  displayName: string;
  experienceYears?: number;
  location: string;
  availabilityStatus: string;
  dailyRate?: number;
  profileDescription?: string;
  isTravelWilling: boolean;
  skills: SkillResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateWorkerProfileRequest {
  displayName: string;
  location: string;
  availabilityStatus: string;
  experienceYears?: number;
  dailyRate?: number;
  profileDescription?: string;
  isTravelWilling: boolean;
}

export const workerService = {
  async createProfile(req: CreateWorkerProfileRequest): Promise<WorkerProfileResponse> {
    return request<WorkerProfileResponse>('/api/workers', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getProfile(): Promise<WorkerProfileResponse> {
    return request<WorkerProfileResponse>('/api/workers/me');
  },

  async updateProfile(req: CreateWorkerProfileRequest): Promise<WorkerProfileResponse> {
    return request<WorkerProfileResponse>('/api/workers/me', {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },

  async addSkill(skillId: string): Promise<WorkerProfileResponse> {
    return request<WorkerProfileResponse>('/api/workers/me/skills', {
      method: 'POST',
      body: JSON.stringify({ skillId }),
    });
  },

  async removeSkill(skillId: string): Promise<WorkerProfileResponse> {
    return request<WorkerProfileResponse>(`/api/workers/me/skills/${skillId}`, {
      method: 'DELETE',
    });
  },
};
