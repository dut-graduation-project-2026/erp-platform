import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';

export interface RoleBaseResponse {
  id: string;
  name: string;
}

export interface PagedEntityResponse<T> {
  data: T[];
  page: number;
  limit: number;
  totalElements: number;
  totalPages: number;
}

export interface PermissionResponse {
  id: string;
  name: string;
  code: string;
  description: string;
}

export interface RoleResponse {
  id: string;
  name: string;
  permissions: PermissionResponse[];
  organization?: {
    id: string;
    name: string;
  };
}

export interface CreateRoleRequest {
  name: string;
  permissionIds: string[];
}

export interface UpdateRoleRequest {
  name: string;
  permissionIds: string[];
}

export const fetchRolesApi = async (orgId: string): Promise<PagedEntityResponse<RoleBaseResponse>> => {
  const response = await apiClient.get<PagedEntityResponse<RoleBaseResponse>>(API_ENDPOINTS.ORGANIZATIONS.ROLES(orgId));
  return response.data;
};

export const fetchRoleDetailApi = async (orgId: string, roleId: string): Promise<RoleResponse> => {
  const response = await apiClient.get<RoleResponse>(`${API_ENDPOINTS.ORGANIZATIONS.ROLES(orgId)}/${roleId}`);
  return response.data;
};

export const createRoleApi = async (orgId: string, data: CreateRoleRequest): Promise<RoleResponse> => {
  const response = await apiClient.post<RoleResponse>(API_ENDPOINTS.ORGANIZATIONS.ROLES(orgId), data);
  return response.data;
};

export const updateRoleApi = async (orgId: string, roleId: string, data: UpdateRoleRequest): Promise<RoleResponse> => {
  const response = await apiClient.put<RoleResponse>(`${API_ENDPOINTS.ORGANIZATIONS.ROLES(orgId)}/${roleId}`, data);
  return response.data;
};
