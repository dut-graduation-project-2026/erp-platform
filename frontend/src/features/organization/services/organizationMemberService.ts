import { apiClient } from '@/services/api-client';

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
      `/organizations/${organizationId}/members`,
      {
        params: { query, page, limit },
      }
    );
    return response.data;
  },

  getMemberById: async (organizationId: string, userId: string): Promise<OrganizationMemberResponse> => {
    const response = await apiClient.get<OrganizationMemberResponse>(
      `/organizations/${organizationId}/members/${userId}`
    );
    return response.data;
  },

  updateMemberRoles: async (organizationId: string, userId: string, roleIds: string[]): Promise<OrganizationMemberResponse> => {
    const response = await apiClient.put<OrganizationMemberResponse>(
      `/organizations/${organizationId}/members/${userId}/roles`,
      { roleIds }
    );
    return response.data;
  },

  removeMember: async (organizationId: string, userId: string): Promise<void> => {
    await apiClient.delete(`/organizations/${organizationId}/members/${userId}`);
  },
};
