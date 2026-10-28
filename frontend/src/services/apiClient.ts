// NirmaanSetu Production Spring Boot API Client
const TOKEN_KEY = 'nirmaansetu_token';

// In web dev mode with Vite proxy, VITE_API_BASE_URL can be empty ("").
// For production website or native Android Capacitor builds, configure VITE_API_BASE_URL (e.g. "https://api.nirmaansetu.in")
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '');

export const getAuthToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

export const setAuthToken = (token: string | null) => {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
};

export interface ApiError {
  status: number;
  message: string;
  code?: string;
}

export async function apiRequest<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = `HTTP error ${response.status}: ${response.statusText}`;
    try {
      const errorJson = await response.json();
      if (errorJson.message) {
        errorMessage = errorJson.message;
      } else if (errorJson.error) {
        errorMessage = errorJson.error;
      }
    } catch (_) {
      // response was not JSON
    }
    const error: ApiError = {
      status: response.status,
      message: errorMessage,
    };
    throw error;
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const apiClient = {
  auth: {
    startOtp: (phoneNumber: string) =>
      apiRequest<{ devMode: boolean; devOtp?: string }>('/api/auth/otp/start', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber }),
      }),
    verifyOtp: (phoneNumber: string, otp: string) =>
      apiRequest<{
        userId: string;
        roles: string[];
        accessToken: string;
        expiresAt: string;
      }>('/api/auth/otp/verify', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, otp }),
      }),
    getMe: () =>
      apiRequest<{ userId: string; phone?: string; roles: string[] }>('/api/auth/me', {
        method: 'GET',
      }),
    onboard: (role: string, displayName?: string) =>
      apiRequest<{ userId: string; phone?: string; roles: string[] }>('/api/auth/onboard', {
        method: 'POST',
        body: JSON.stringify({ role, displayName }),
      }),
  },

  workers: {
    getMyProfile: () => apiRequest<any>('/api/workers/me'),
    createProfile: (data: { displayName: string; location?: string; skillIds?: string[] }) =>
      apiRequest<any>('/api/workers', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    updateProfile: (data: { displayName?: string; location?: string; availabilityStatus?: string }) =>
      apiRequest<any>('/api/workers/me', {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
    getAllWorkers: () => apiRequest<any[]>('/api/workers/all'),
  },

  clients: {
    getMyProfile: () => apiRequest<any>('/api/clients/profile/me'),
    createProfile: (data: { displayName?: string; clientType?: string; companyName?: string; gstNumber?: string; billingAddress?: string }) =>
      apiRequest<any>('/api/clients/profile', {
        method: 'POST',
        body: JSON.stringify({
          clientType: data.clientType || 'BUILDER',
          displayName: data.displayName || data.companyName || 'Client Profile',
        }),
      }),
    updateProfile: (data: { companyName?: string; gstNumber?: string; billingAddress?: string }) =>
      apiRequest<any>('/api/clients/profile/me', {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
  },

  contractors: {
    getMyProfile: () => apiRequest<any>('/api/contractors/profile/me'),
    createProfile: (data: { companyName: string; licenseNumber?: string; gstNumber?: string }) =>
      apiRequest<any>('/api/contractors/profile', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    getAssociatedWorkers: () => apiRequest<any[]>('/api/contractors/workers'),
    associateWorker: (workerProfileId: string, startsOn: string, endsOn?: string, notes?: string) =>
      apiRequest<any>('/api/contractors/workers', {
        method: 'POST',
        body: JSON.stringify({ workerProfileId, startsOn, endsOn, notes }),
      }),
  },

  projects: {
    getMyProjects: () => apiRequest<any[]>('/api/projects'),
    getAllProjects: () => apiRequest<any[]>('/api/projects/all'),
    getProjectById: (projectId: string) => apiRequest<any>(`/api/projects/${projectId}`),
    createProject: (data: { title: string; description?: string; location?: string; budget?: number; startDate?: string; endDate?: string }) =>
      apiRequest<any>('/api/projects', {
        method: 'POST',
        body: JSON.stringify({
          title: data.title,
          description: data.description || '',
          location: data.location || 'India',
          startDate: data.startDate || new Date().toISOString().split('T')[0],
        }),
      }),
    updateProject: (projectId: string, data: any) =>
      apiRequest<any>(`/api/projects/${projectId}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      }),
  },

  sites: {
    getSites: () => apiRequest<any[]>('/api/sites'),
    createSite: (data: {
      projectId?: string;
      projectTitle?: string;
      name: string;
      address?: string;
      city?: string;
      state?: string;
      latitude: number;
      longitude: number;
      geofenceRadiusMeters?: number;
      managerName?: string;
      contactPhone?: string;
    }) =>
      apiRequest<any>('/api/sites', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    deleteSite: (id: string) =>
      apiRequest<void>(`/api/sites/${id}`, {
        method: 'DELETE',
      }),
  },

  milestones: {
    getByProject: (projectId: string) => apiRequest<any[]>(`/api/milestones/project/${projectId}`),
    create: (data: {
      projectId: string;
      phaseName: string;
      description?: string;
      targetDate?: string;
      status?: string;
      orderIndex?: number;
    }) =>
      apiRequest<any>('/api/milestones', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    updateStatus: (id: string, status: string, completionDate?: string) =>
      apiRequest<any>(`/api/milestones/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status, completionDate }),
      }),
  },

  attendance: {
    checkIn: (data: {
      projectId?: string;
      projectTitle?: string;
      siteId: string;
      siteName?: string;
      latitude: number;
      longitude: number;
      selfieUrl?: string;
      remarks?: string;
    }) =>
      apiRequest<any>('/api/attendance/check-in', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    checkOut: (data?: { remarks?: string }) =>
      apiRequest<any>('/api/attendance/check-out', {
        method: 'POST',
        body: JSON.stringify(data || {}),
      }),
    getMyAttendance: () => apiRequest<any[]>('/api/attendance/me'),
    getAllAttendance: () => apiRequest<any[]>('/api/attendance'),
    review: (id: string, approvalStatus: 'APPROVED' | 'REJECTED', remarks?: string) =>
      apiRequest<any>(`/api/attendance/${id}/approval`, {
        method: 'PATCH',
        body: JSON.stringify({ approvalStatus, remarks }),
      }),
  },

  salaries: {
    getMySalaries: () => apiRequest<any[]>('/api/salaries/me'),
    getAllSalaries: () => apiRequest<any[]>('/api/salaries/all'),
    generate: (data: {
      workerId: string;
      workerName: string;
      month: string;
      daysPresent: number;
      halfDays?: number;
      overtimeHours?: number;
      dailyRate: number;
      bonus?: number;
      deductions?: number;
    }) =>
      apiRequest<any>('/api/salaries/generate', {
        method: 'POST',
        body: JSON.stringify({
          workerUserId: data.workerId,
          workerName: data.workerName,
          salaryMonth: data.month,
          daysPresent: data.daysPresent,
          halfDays: data.halfDays || 0,
          overtimeHours: data.overtimeHours || 0,
          dailyRate: data.dailyRate,
          bonus: data.bonus || 0,
          deductions: data.deductions || 0,
        }),
      }),
    markPaid: (id: string, paymentMethod: string, paymentRef: string) =>
      apiRequest<any>(`/api/salaries/${id}/pay`, {
        method: 'PATCH',
        body: JSON.stringify({ paymentMethod, paymentRef }),
      }),
  },

  invoices: {
    getMyInvoices: () => apiRequest<any[]>('/api/invoices/my'),
    getAllInvoices: () => apiRequest<any[]>('/api/invoices/all'),
    create: (data: {
      projectId: string;
      projectTitle: string;
      clientId?: string;
      clientName?: string;
      amount: number;
      taxAmount?: number;
      dueDate?: string;
      description?: string;
    }) =>
      apiRequest<any>('/api/invoices', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    markPaid: (id: string, paymentMethod: string, transactionRef: string) =>
      apiRequest<any>(`/api/invoices/${id}/pay`, {
        method: 'PATCH',
        body: JSON.stringify({ paymentMethod, transactionRef }),
      }),
  },

  materials: {
    getMaterials: () => apiRequest<any[]>('/api/materials'),
    create: (data: {
      projectId: string;
      projectTitle?: string;
      siteId: string;
      itemName: string;
      category?: string;
      quantity: number;
      unit?: string;
      minThreshold?: number;
      unitCost?: number;
      supplierName?: string;
      supplierContact?: string;
    }) =>
      apiRequest<any>('/api/materials', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    updateStock: (id: string, quantity: number) =>
      apiRequest<any>(`/api/materials/${id}/stock`, {
        method: 'PATCH',
        body: JSON.stringify({ newQuantity: quantity }),
      }),
    delete: (id: string) =>
      apiRequest<void>(`/api/materials/${id}`, {
        method: 'DELETE',
      }),
  },

  expenses: {
    getExpenses: () => apiRequest<any[]>('/api/expenses'),
    create: (data: {
      projectId: string;
      projectTitle?: string;
      siteId?: string;
      category: string;
      amount: number;
      description: string;
      billReceiptUrl?: string;
      paidTo: string;
      paymentMode?: string;
      date?: string;
    }) =>
      apiRequest<any>('/api/expenses', {
        method: 'POST',
        body: JSON.stringify(data),
      }),
    review: (id: string, status: 'APPROVED' | 'REJECTED') =>
      apiRequest<any>(`/api/expenses/${id}/review`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      }),
  },

  dpr: {
    getDprs: () => apiRequest<any[]>('/api/dpr'),
    create: (data: {
      projectId?: string;
      projectTitle?: string;
      siteId: string;
      siteName?: string;
      reportDate?: string;
      reportedBy?: string;
      weatherCondition?: string;
      skilledLaborCount?: number;
      unskilledLaborCount?: number;
      tasksCompleted: string;
      delaysOrIssues?: string;
      safetyObservations?: string;
      photos?: string[] | string;
    }) =>
      apiRequest<any>('/api/dpr', {
        method: 'POST',
        body: JSON.stringify({
          ...data,
          reportDate: data.reportDate || new Date().toISOString().split('T')[0],
          weatherCondition: data.weatherCondition || 'Sunny',
          photos: Array.isArray(data.photos) ? data.photos.join(',') : (data.photos || ''),
        }),
      }),
    approve: (id: string) =>
      apiRequest<any>(`/api/dpr/${id}/approve`, {
        method: 'PATCH',
      }),
  },

  audit: {
    getAuditLogs: () => apiRequest<any[]>('/api/audit-logs'),
  },

  notifications: {
    getMyNotifications: () =>
      apiRequest<any>('/api/notifications').then((res) => (res.content ? res.content : res)),
    getUnreadCount: () =>
      apiRequest<{ unreadCount: number }>('/api/notifications/unread-count').then((res) => res.unreadCount),
    markAsRead: (id: string) =>
      apiRequest<any>(`/api/notifications/${id}/read`, {
        method: 'POST',
      }),
    markAllAsRead: () =>
      apiRequest<{ markedReadCount: number }>('/api/notifications/read-all', {
        method: 'POST',
      }),
  },

  uploads: {
    uploadBase64: (dataUrl: string) =>
      apiRequest<{ url: string; filename: string }>('/api/uploads/base64', {
        method: 'POST',
        body: JSON.stringify({ dataUrl }),
      }),
    uploadImage: async (file: File) => {
      const formData = new FormData();
      formData.append('file', file);
      return apiRequest<{ url: string; filename: string }>('/api/uploads/image', {
        method: 'POST',
        body: formData,
      });
    },
  },
};
