import { LoginRequest, RegisterRequest } from "@/types/auth";
import type { User } from "@/types/user";
import type { UserOrganization } from "@/types/organization";
import { apiClient } from "@/services/api-client";
import { API_ENDPOINTS } from "@/config/constants";

export const login = (payload: LoginRequest): Promise<void> =>
  apiClient.post(API_ENDPOINTS.AUTH.LOGIN, payload);

export const register = (payload: RegisterRequest): Promise<void> =>
  apiClient.post(API_ENDPOINTS.AUTH.REGISTER, payload);

export const logout = (): Promise<void> =>
  apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);

export const getCurrentUser = async (): Promise<User> => {
  const response = await apiClient.get(API_ENDPOINTS.AUTH.PROFILE);
  return response.data;
};

export const getUserOrganizations = async (): Promise<UserOrganization[]> => {
  const response = await apiClient.get(API_ENDPOINTS.ORGANIZATIONS.ME);
  return response.data;
};

export const refreshToken = (): Promise<void> =>
  apiClient.post(API_ENDPOINTS.AUTH.REFRESH);
