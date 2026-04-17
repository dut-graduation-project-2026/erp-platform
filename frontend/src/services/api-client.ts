import axios, { type AxiosRequestConfig } from 'axios';
import { env } from '@/config/env';
import { useAuthStore } from '@/store/use-auth-store';

export interface AppRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
}

export const apiClient = axios.create({
  withCredentials: true,
  baseURL: env.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Attach X-Org-Id header
apiClient.interceptors.request.use(
  (config) => {
    const { currentOrgId } = useAuthStore.getState();
    if (currentOrgId) {
      config.headers['X-Org-Id'] = currentOrgId;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401 errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Logout and redirect
      useAuthStore.getState().clearAuth();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
