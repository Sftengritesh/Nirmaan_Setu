export interface ApiError {
  code: string;
  message: string;
}

const TOKEN_KEY = 'nirmaansetu_session_token';

export const getStoredToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
};

export const setStoredToken = (token: string, rememberMe = true) => {
  if (rememberMe) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    sessionStorage.setItem(TOKEN_KEY, token);
  }
};

export const removeStoredToken = () => {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
};

export async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(endpoint, {
    ...options,
    headers: headers as HeadersInit,
  });

  if (response.status === 204) {
    return {} as T;
  }

  let data;
  try {
    data = await response.json();
  } catch (err) {
    data = null;
  }

  if (!response.ok) {
    if (response.status === 401) {
      removeStoredToken();
    }
    const error: ApiError = {
      code: data?.code || 'UNKNOWN_ERROR',
      message: data?.message || `Request failed with status ${response.status}`,
    };
    throw error;
  }

  return data as T;
}
