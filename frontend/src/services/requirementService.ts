import { request } from './apiClient';

export interface WorkforceRequirementResponse {
  id: string;
  projectId: string;
  location: string;
  startDate: string;
  durationDays: number;
  workerType: string;
  skillId: string;
  quantity: number;
  dailyRate?: number;
  budgetAmount?: number;
  currencyCode?: string;
  accommodationAvailable?: boolean;
  foodAvailable?: boolean;
  additionalNotes?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRequirementRequest {
  location: string;
  startDate: string;
  durationDays: number;
  workerType: string;
  skillId: string;
  quantity: number;
  dailyRate?: number;
  budgetAmount?: number;
  currencyCode?: string;
  accommodationAvailable?: boolean;
  foodAvailable?: boolean;
  additionalNotes?: string;
}

export const requirementService = {
  async createRequirement(projectId: string, req: CreateRequirementRequest): Promise<WorkforceRequirementResponse> {
    return request<WorkforceRequirementResponse>(`/api/projects/${projectId}/requirements`, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getRequirementsByProject(projectId: string): Promise<WorkforceRequirementResponse[]> {
    return request<WorkforceRequirementResponse[]>(`/api/projects/${projectId}/requirements`);
  },

  async getRequirementById(requirementId: string): Promise<WorkforceRequirementResponse> {
    return request<WorkforceRequirementResponse>(`/api/requirements/${requirementId}`);
  },

  async updateRequirement(requirementId: string, req: CreateRequirementRequest): Promise<WorkforceRequirementResponse> {
    return request<WorkforceRequirementResponse>(`/api/requirements/${requirementId}`, {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },

  async openRequirement(requirementId: string): Promise<WorkforceRequirementResponse> {
    return request<WorkforceRequirementResponse>(`/api/requirements/${requirementId}/open`, {
      method: 'POST',
    });
  },

  async cancelRequirement(requirementId: string): Promise<WorkforceRequirementResponse> {
    return request<WorkforceRequirementResponse>(`/api/requirements/${requirementId}/cancel`, {
      method: 'POST',
    });
  },
};
