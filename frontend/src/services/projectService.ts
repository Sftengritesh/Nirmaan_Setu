import { request } from './apiClient';

export interface ProjectResponse {
  id: string;
  clientProfileId: string;
  title: string;
  description?: string;
  location: string;
  status: string;
  startDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProjectRequest {
  title: string;
  description?: string;
  location: string;
  startDate?: string;
}

export const projectService = {
  async createProject(req: CreateProjectRequest): Promise<ProjectResponse> {
    return request<ProjectResponse>('/api/projects', {
      method: 'POST',
      body: JSON.stringify(req),
    });
  },

  async getMyProjects(): Promise<ProjectResponse[]> {
    return request<ProjectResponse[]>('/api/projects');
  },

  async getProjectById(projectId: string): Promise<ProjectResponse> {
    return request<ProjectResponse>(`/api/projects/${projectId}`);
  },

  async updateProject(projectId: string, req: CreateProjectRequest): Promise<ProjectResponse> {
    return request<ProjectResponse>(`/api/projects/${projectId}`, {
      method: 'PUT',
      body: JSON.stringify(req),
    });
  },
};
