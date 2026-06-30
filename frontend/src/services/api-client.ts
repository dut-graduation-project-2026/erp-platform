import axios, { type AxiosRequestConfig } from 'axios';
import { env } from '@/config/env';
import { useAuthStore } from '@/store/use-auth-store';
import { toast } from 'sonner';

export interface AppRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
  _retry?: boolean;
  _isRetryAttempt?: boolean;
  _permissionRetry?: boolean;
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
    const requestConfig = config as AppRequestConfig;
    const { currentOrgId } = useAuthStore.getState();

    // Ensure stale retry state from reused config objects never leaks into a new request.
    if (!requestConfig._isRetryAttempt) {
      requestConfig._retry = false;
    } else {
      requestConfig._isRetryAttempt = false;
    }
    
    // Gắn X-Org-Id header nếu có currentOrgId
    if (currentOrgId) {
      config.headers['X-Org-Id'] = currentOrgId;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Flag to track if a refresh is currently in progress
let isRefreshing = false;
// Queue of failed requests waiting for the token to be refreshed
let failedQueue: Array<{ resolve: (value?: unknown) => void; reject: (reason?: any) => void }> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

/**
 * Response Interceptor: Handle 401 (token expired) và 403 (access denied)
 */
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as AppRequestConfig | undefined;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    // 🟠 BƯỚC 4: Handle 401 - Token expired, attempt refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Do not try to refresh or redirect if the request was to the login or register endpoint
      const isAuthEndpoint =
        originalRequest.url?.includes('/auth/login') ||
        originalRequest.url?.includes('/auth/register') ||
        originalRequest.url?.includes('/auth/refresh') ||
        originalRequest.skipAuth;
      
      if (!isAuthEndpoint) {
        if (isRefreshing) {
          // If refresh is already in progress, queue the request
          return new Promise(function (resolve, reject) {
            failedQueue.push({ resolve, reject });
          })
            .then(() => {
              return apiClient(originalRequest);
            })
            .catch((err) => {
              return Promise.reject(err);
            });
        }

        originalRequest._retry = true;
        originalRequest._isRetryAttempt = true;
        isRefreshing = true;

        try {
          console.log("ATTEMPTING TO REFRESH TOKEN...");
          // Attempt to refresh token (HttpOnly cookie will be automatically sent)
          const refreshRes = await axios.post(
            `${env.API_BASE_URL}/auth/refresh`,
            {},
            { withCredentials: true }
          );
          console.log("REFRESH TOKEN SUCCESS!", refreshRes.data);
          
          processQueue(null, 'Success');
          // Retry the original request with new token
          return apiClient(originalRequest);
        } catch (refreshError: any) {
          console.error("REFRESH TOKEN FAILED:", refreshError);
          processQueue(refreshError, null);
          // Refresh failed, logout and redirect to login
          useAuthStore.getState().clearAuth();
          if (window.location.pathname !== '/login') {
            window.location.href = '/login';
            toast.error('Session expired. Please login again.');
          }
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }
    }

    // 🟠 BƯỚC 4: Handle 403 - Access denied (permission issue)
    if (error.response?.status === 403) {
      if (!originalRequest._permissionRetry) {
        originalRequest._permissionRetry = true;
        const { currentOrgId, setPermissions } = useAuthStore.getState();
        if (currentOrgId) {
          try {
            console.log("403 FORBIDDEN ENCOUNTERED. RE-FETCHING PERMISSIONS TO SYNC...");
            const { fetchMyPermissionsApi } = await import('@/features/auth/services/authService');
            const newPermissions = await fetchMyPermissionsApi(currentOrgId);
            setPermissions(newPermissions);
            console.log("PERMISSIONS SYNCED SUCCESSFULLY. UI will update via PermissionGuard.");
            // Do NOT retry the request — the permission is genuinely revoked.
            // Let PermissionGuard re-render and show <AccessDenied /> instead.
            toast.error('Access permissions have been changed. The interface has been updated..');
            return Promise.reject(error);
          } catch (permError) {
            console.error("Failed to sync permissions on 403:", permError);
          }
        }
      }
      toast.error(error.response.data?.message || 'Access denied. You do not have permission to perform this action.');
      return Promise.reject(error);
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
