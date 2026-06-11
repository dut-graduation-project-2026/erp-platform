import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';

export interface RoleResponse {
  id: string;
  name: string;
  description?: string;
}

export interface OrganizationMemberResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: RoleResponse[];
  status: string;
  lastLogin: string | null;
}

export interface PagedEntityResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    totalItems: number;
    totalPages: number;
    hasNext: boolean;
    hasPrev: boolean;
  };
}

export const organizationMemberService = {
  getMembers: async (
    organizationId: string,
    query?: string,
    page: number = 1,
    limit: number = 10
  ): Promise<PagedEntityResponse<OrganizationMemberResponse>> => {
    const response = await apiClient.get<PagedEntityResponse<OrganizationMemberResponse>>(
      API_ENDPOINTS.USERS.BASE,
      {
        params: { organizationId, query, page, limit },
      }
    );
    return response.data;
  },

  getMemberById: async (organizationId: string, userId: string): Promise<OrganizationMemberResponse> => {
    const response = await apiClient.get<OrganizationMemberResponse>(
      API_ENDPOINTS.USERS.DETAIL(userId),
      {
        params: { organizationId }
      }
    );
    return response.data;
  },

  updateMemberRoles: async (organizationId: string, userId: string, roleIds: string[]): Promise<OrganizationMemberResponse> => {
    const response = await apiClient.put<OrganizationMemberResponse>(
      API_ENDPOINTS.USERS.ROLES(userId),
      { organizationId, roleIds }
    );
    return response.data;
  },

  removeMember: async (organizationId: string, userId: string): Promise<void> => {
    await apiClient.delete(API_ENDPOINTS.USERS.DETAIL(userId), {
      params: { organizationId }
    });
  },
};
