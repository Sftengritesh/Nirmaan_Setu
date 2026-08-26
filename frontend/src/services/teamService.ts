import { request } from './apiClient';

export interface TeamResponse {
  id: string;
  managerUserId: string;
  name: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface TeamMemberResponse {
  teamId: string;
  workerProfileId: string;
  startsOn: string;
  endsOn?: string;
}

export interface CreateTeamRequest {
  name: string;
  description?: string;
}

export interface AddTeamMemberRequest {
  workerProfileId: string;
  startsOn: string;
  endsOn?: string;
}

export const teamService = {
  async createTeam(req: CreateTeamRequest): Promise<TeamResponse> {
    return request<TeamResponse>('/api/teams', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getMyTeams(): Promise<TeamResponse[]> {
    return request<TeamResponse[]>('/api/teams');
  },

  async getTeamById(teamId: string): Promise<TeamResponse> {
    return request<TeamResponse>(`/api/teams/${teamId}`);
  },

  async updateTeam(teamId: string, req: CreateTeamRequest): Promise<TeamResponse> {
    return request<TeamResponse>(`/api/teams/${teamId}`, {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },

  async addTeamMember(teamId: string, req: AddTeamMemberRequest): Promise<TeamMemberResponse> {
    return request<TeamMemberResponse>(`/api/teams/${teamId}/members`, {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getTeamMembers(teamId: string): Promise<TeamMemberResponse[]> {
    return request<TeamMemberResponse[]>(`/api/teams/${teamId}/members`);
  },

  async removeTeamMember(teamId: string, workerProfileId: string, startsOn: string, endsOn?: string): Promise<TeamMemberResponse> {
    const params = new URLSearchParams({ startsOn });
    if (endsOn) params.append('endsOn', endsOn);
    return request<TeamMemberResponse>(`/api/teams/${teamId}/members/${workerProfileId}/end?${params.toString()}`, {
      method: 'POST',
    });
  },
};
