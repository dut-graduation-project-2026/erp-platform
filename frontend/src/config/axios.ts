'use client';

import axios, { HttpStatusCode, type AxiosRequestConfig } from 'axios';
import { toast } from 'sonner';
import { env } from '@/config/env';

export interface AppRequestConfig extends AxiosRequestConfig {
  skipAuth?: boolean;
  _retry?: boolean;
}

export const instance = axios.create({
  withCredentials: true,
  baseURL: env.API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshHandler: (() => Promise<void>) | null = null;
let onAuthFailure: (() => Promise<void>) | null = null;

export function registerAuthHandlers(handlers: {
  refresh: () => Promise<void>;
  onAuthFailure: () => Promise<void>;
}) {
  refreshHandler = handlers.refresh;
  onAuthFailure = handlers.onAuthFailure;
}

let refreshPromise: Promise<void> | null = null;
let isAuthFailing = false;

const triggerAuthFailure = async () => {
  if (isAuthFailing) return;
  isAuthFailing = true;

  try {
    await onAuthFailure?.();
  } finally {
    isAuthFailing = false;
  }
};

const showErrorToast = (error: any) => {
  if (!error.response) {
    toast.error('Lỗi mạng', { description: 'Không thể kết nối đến máy chủ.' });
    return;
  }

  const data = error.response.data;
  // Backend returns: { code, message, details }
  if (data && data.message) {
    // If there are validation details, we could format them, but simple message is good for global
    toast.error(data.message);
  } else {
    toast.error(`Lỗi hệ thống (${error.response.status})`, { 
      description: 'Đã có lỗi xảy ra. Vui lòng thử lại sau.' 
    });
  }
};

instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as AppRequestConfig | undefined;

    if (!error.response) {
      showErrorToast(error);
      await triggerAuthFailure();
      return Promise.reject(error);
    }

    if (!originalRequest) {
      showErrorToast(error);
      return Promise.reject(error);
    }

    if (originalRequest.skipAuth) {
      showErrorToast(error);
      return Promise.reject(error);
    }

    const status = error.response.status;
    const isUnauthorized = status === HttpStatusCode.Unauthorized;

    if (isUnauthorized && originalRequest._retry) {
      showErrorToast(error);
      await triggerAuthFailure();
      return Promise.reject(error);
    }

    if (isUnauthorized && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        if (!refreshPromise) {
          if (!refreshHandler) {
            await triggerAuthFailure();
            return Promise.reject(error);
          }

          refreshPromise = refreshHandler()
            .catch(async (refreshError) => {
              await triggerAuthFailure();
              return Promise.reject(refreshError);
            })
            .finally(() => {
              refreshPromise = null;
            });
        }

        await refreshPromise;

        return instance(originalRequest);
      } catch (err) {
        showErrorToast(err);
        return Promise.reject(err);
      }
    }

    showErrorToast(error);
    return Promise.reject(error);
  }
);
