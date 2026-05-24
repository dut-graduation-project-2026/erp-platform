import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import { PagedEntityResponse } from './roleService';

export interface UserBaseResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  // Fields that might be added by backend later or for UI mockup
  role?: string;
  status?: string;
  lastLogin?: string;
}

export interface FetchUsersParams {
  organizationId: string;
  query?: string;
  page?: number;
  limit?: number;
}

export const fetchUsersApi = async (params: FetchUsersParams): Promise<PagedEntityResponse<UserBaseResponse>> => {
  const { organizationId, query, page = 1, limit = 10 } = params;
  const response = await apiClient.get<PagedEntityResponse<UserBaseResponse>>(API_ENDPOINTS.USERS.BASE, {
    params: {
      organizationId,
      query,
      page,
      limit,
    },
  });
  return response.data;
};
