import { request } from './apiClient';
import { WorkerProfileResponse } from './workerService';
import { ContractorProfileResponse } from './contractorService';
import { TeamResponse } from './teamService';
import { WorkforceRequirementResponse } from './requirementService';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export const discoveryService = {
  async searchWorkers(params: Record<string, any>): Promise<PageResponse<WorkerProfileResponse>> {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        searchParams.append(key, String(val));
      }
    });
    return request<PageResponse<WorkerProfileResponse>>(`/api/discovery/workers?${searchParams.toString()}`);
  },

  async searchContractors(params: Record<string, any>): Promise<PageResponse<ContractorProfileResponse>> {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        searchParams.append(key, String(val));
      }
    });
    return request<PageResponse<ContractorProfileResponse>>(`/api/discovery/contractors?${searchParams.toString()}`);
  },

  async searchTeams(params: Record<string, any>): Promise<PageResponse<TeamResponse>> {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        searchParams.append(key, String(val));
      }
    });
    return request<PageResponse<TeamResponse>>(`/api/discovery/teams?${searchParams.toString()}`);
  },

  async searchRequirements(params: Record<string, any>): Promise<PageResponse<WorkforceRequirementResponse>> {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        searchParams.append(key, String(val));
      }
    });
    return request<PageResponse<WorkforceRequirementResponse>>(`/api/discovery/requirements?${searchParams.toString()}`);
  },
};
