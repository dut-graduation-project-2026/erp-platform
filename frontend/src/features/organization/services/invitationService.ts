import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS, InvitationStatus } from '@/config/constants';

export interface OrganizationInvitationUserRequest {
  roleId: string;
  email: string;
}

export interface UpdateOrganizationInvitationStatusRequest {
  accepted: boolean;
}

export interface OrganizationInvitationResponse {
  id: string;
  email: string;
  roleId: string;
  organizationId: string;
  status: InvitationStatus;
  createdAt: string;
}

export interface BulkOrganizationInvitationRequest {
  roleId: string;
  emails: string[];
}

export const inviteUserApi = async (
  orgId: string,
  data: OrganizationInvitationUserRequest
): Promise<OrganizationInvitationResponse> => {
  const response = await apiClient.post<OrganizationInvitationResponse>(
    API_ENDPOINTS.ORGANIZATIONS.INVITATIONS(orgId),
    data
  );
  return response.data;
};

export const bulkInviteUsersApi = async (
  orgId: string,
  data: BulkOrganizationInvitationRequest
): Promise<OrganizationInvitationResponse[]> => {
  const response = await apiClient.post<OrganizationInvitationResponse[]>(
    `${API_ENDPOINTS.ORGANIZATIONS.INVITATIONS(orgId)}/bulk`,
    data
  );
  return response.data;
};

export const getInvitationsApi = async (
  orgId: string
): Promise<OrganizationInvitationResponse[]> => {
  const response = await apiClient.get<OrganizationInvitationResponse[]>(
    API_ENDPOINTS.ORGANIZATIONS.INVITATIONS(orgId)
  );
  return response.data;
};


export const resendInvitationApi = async (
  orgId: string,
  invitationId: string
): Promise<OrganizationInvitationResponse> => {
  const response = await apiClient.post<OrganizationInvitationResponse>(
    `${API_ENDPOINTS.ORGANIZATIONS.INVITATIONS(orgId)}/${invitationId}/resend`
  );
  return response.data;
};

export const respondToInvitationApi = async (
  orgId: string,
  invitationId: string,
  data: UpdateOrganizationInvitationStatusRequest
): Promise<OrganizationInvitationResponse> => {
  const response = await apiClient.patch<OrganizationInvitationResponse>(
    `${API_ENDPOINTS.ORGANIZATIONS.INVITATIONS(orgId)}/${invitationId}`,
    data
  );
  return response.data;
};
