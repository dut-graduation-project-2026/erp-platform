import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';

export interface CreateOrganizationRequest {
  name: string;
  description?: string;
  address: string;
  hotline: string;
  taxCode: string;
}

export interface UpdateOrganizationRequest {
  name: string;
  description?: string;
  address: string;
  hotline: string;
  taxCode: string;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  taxCode: string;
  hotline?: string;
  address?: string;
  description?: string;
  createdAt?: string;
}

export const createOrganizationApi = async (
  payload: CreateOrganizationRequest
): Promise<OrganizationResponse> => {
  const response = await apiClient.post<OrganizationResponse>(API_ENDPOINTS.ORGANIZATIONS.BASE, payload);
  return response.data;
};

export const fetchMyOrganizationsApi = async (): Promise<OrganizationResponse[]> => {
  const response = await apiClient.get<OrganizationResponse[]>(API_ENDPOINTS.ORGANIZATIONS.ME);
  return response.data;
};

export const updateOrganizationApi = async (
  orgId: string,
  payload: UpdateOrganizationRequest
): Promise<OrganizationResponse> => {
  const response = await apiClient.put<OrganizationResponse>(`${API_ENDPOINTS.ORGANIZATIONS.BASE}/${orgId}`, payload);
  return response.data;
};

export const fetchOrganizationByIdApi = async (
  orgId: string
): Promise<OrganizationResponse> => {
  const response = await apiClient.get<OrganizationResponse>(`${API_ENDPOINTS.ORGANIZATIONS.BASE}/${orgId}`);
  return response.data;
};

