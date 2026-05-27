'use client';

import { apiClient, type AppRequestConfig } from '@/services/api-client';

export type { AppRequestConfig };

// Backward-compatible export: the project now uses a single shared client.
export const instance = apiClient;

// Legacy no-op to preserve compatibility for old call sites.
export function registerAuthHandlers() {
  return;
}
