import { apiClient, type AppRequestConfig } from "@/services/api-client";

async function apiRequest<T>(config: AppRequestConfig): Promise<T> {
  const response = await apiClient.request<T>(config);
  return response.data;
}

export function apiGet<T>(url: string, config?: AppRequestConfig): Promise<T> {
  return apiRequest<T>({ ...(config ?? {}), method: "GET", url });
}

export function apiPost<T>(
  url: string,
  data?: unknown,
  config?: AppRequestConfig,
): Promise<T> {
  return apiRequest<T>({ ...(config ?? {}), method: "POST", url, data });
}

export function apiPut<T>(
  url: string,
  data?: unknown,
  config?: AppRequestConfig,
): Promise<T> {
  return apiRequest<T>({ ...(config ?? {}), method: "PUT", url, data });
}

export function apiPatch<T>(
  url: string,
  data?: unknown,
  config?: AppRequestConfig,
): Promise<T> {
  return apiRequest<T>({ ...(config ?? {}), method: "PATCH", url, data });
}

export function apiDelete<T>(
  url: string,
  data?: unknown,
  config?: AppRequestConfig,
): Promise<T> {
  return apiRequest<T>({ ...(config ?? {}), method: "DELETE", url, data });
}
