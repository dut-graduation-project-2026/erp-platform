import axios, { type AxiosRequestConfig } from 'axios';
import { env } from '@/config/env';
import { useAuthStore } from '@/store/use-auth-store';
import { toast } from 'sonner';

export interface AppRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
}

export const apiClient = axios.create({
  withCredentials: true, // Critical: Allows browser to send HttpOnly cookies with cross-origin requests
  // Without this, even if cookies exist, browser blocks sending them for security (CORS policy)
  baseURL: env.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 🟠 BƯỚC 4: DUY TRÌ PHIÊN & BẢO MẬT (API INTERCEPTOR)
 * 
 * Request Interceptor: Attach X-Org-Id header tự động
 * - Mọi request API sau login sẽ tự động lấy currentOrgId từ Zustand
 * - Gắn vào Header X-Org-Id để backend biết user đang làm việc với org nào
 */
apiClient.interceptors.request.use(
  (config) => {
    const { currentOrgId } = useAuthStore.getState();
    
    // Gắn X-Org-Id header nếu có currentOrgId
    if (currentOrgId) {
      config.headers['X-Org-Id'] = currentOrgId;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response Interceptor: Handle 401 (token expired) và 403 (access denied)
 */
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 🟠 BƯỚC 4: Handle 401 - Token expired, attempt refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      // Check if refresh token cookie exists before attempting refresh
      const hasRefreshToken = document.cookie.includes('refresh_token');
      
      if (!hasRefreshToken) {
        // No refresh token, logout immediately
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        toast.error('Session expired. Please login again.');
        return Promise.reject(error);
      }
      
      try {
        // Attempt to refresh token using refresh token cookie
        await axios.post(
          `${env.API_BASE_URL}/auth/refresh`,
          {},
          { withCredentials: true }
        );
        
        // Retry the original request with new token
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, logout and redirect to login
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        toast.error('Session expired. Please login again.');
        return Promise.reject(refreshError);
      }
    }

    // 🟠 BƯỚC 4: Handle 403 - Access denied (permission issue)
    if (error.response?.status === 403) {
      toast.error('Access denied. You do not have permission to perform this action.');
    }

    // Generic error handling for other HTTP errors
    if (error.response) {
      const message = error.response.data?.message || `Request failed with status ${error.response.status}`;
      toast.error(message);
    } else if (error.request) {
      toast.error('Network error. Please check your connection.');
    } else {
      toast.error('An unexpected error occurred.');
    }

    return Promise.reject(error);
  }
);
