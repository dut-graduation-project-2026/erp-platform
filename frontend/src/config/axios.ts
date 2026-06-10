'use client';

import { apiClient, type AppRequestConfig } from '@/services/api-client';

export type { AppRequestConfig };

// Backward-compatible export: the project now uses a single shared client.
export const instance = apiClient;

type LegacyAuthHandlers = {
  refresh: () => Promise<void>;
  onAuthFailure: () => Promise<void>;
};

// Legacy no-op to preserve compatibility for old call sites.
export function registerAuthHandlers(_handlers: LegacyAuthHandlers) {
  return;
}
