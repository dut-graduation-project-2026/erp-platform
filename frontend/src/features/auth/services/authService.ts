import { LoginRequest } from "@/types/auth";
import type { User } from "@/types/user";
import type { UserOrganization } from "@/types/organization";
import { apiClient } from "@/services/api-client";
import { API_ENDPOINTS } from "@/config/constants";

export const login = async (payload: LoginRequest): Promise<User> =>
  apiClient.post(API_ENDPOINTS.AUTH.LOGIN, payload).then(res => res.data);

export const fetchMyOrganizations = async (): Promise<UserOrganization[]> => {
  const response = await apiClient.get(API_ENDPOINTS.AUTH.ORGANIZATIONS);
  return response.data;
};

export const fetchCurrentUser = async (): Promise<User> => {
  const response = await apiClient.get(API_ENDPOINTS.AUTH.PROFILE);
  return response.data;
};
