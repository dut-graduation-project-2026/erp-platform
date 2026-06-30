import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import { CrmLead, CreateCrmLeadRequest, CrmAppointment } from '../types';

export interface PagedEntityResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export const getLeads = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PagedEntityResponse<CrmLead>> => {
  const response = await apiClient.get<PagedEntityResponse<CrmLead>>(
    API_ENDPOINTS.CRM.LEADS(orgId),
    { params }
  );
  return response.data;
};

export const getLeadById = async (orgId: string, leadId: string): Promise<CrmLead> => {
  const response = await apiClient.get<CrmLead>(`${API_ENDPOINTS.CRM.LEADS(orgId)}/${leadId}`);
  return response.data;
};

export const createLead = async (orgId: string, data: CreateCrmLeadRequest): Promise<CrmLead> => {
  const response = await apiClient.post<CrmLead>(API_ENDPOINTS.CRM.LEADS(orgId), data);
  return response.data;
};

export const updateLead = async (orgId: string, leadId: string, data: Partial<CreateCrmLeadRequest>): Promise<CrmLead> => {
  const response = await apiClient.put<CrmLead>(`${API_ENDPOINTS.CRM.LEADS(orgId)}/${leadId}`, data);
  return response.data;
};

export const updateLeadStage = async (orgId: string, leadId: string, stage: string): Promise<CrmLead> => {
  const response = await apiClient.patch<CrmLead>(`${API_ENDPOINTS.CRM.LEADS(orgId)}/${leadId}/stage`, { stage });
  return response.data;
};

export const deleteLead = async (orgId: string, leadId: string): Promise<void> => {
  await apiClient.delete(`${API_ENDPOINTS.CRM.LEADS(orgId)}/${leadId}`);
};

// Appointments
export const getAppointments = async (orgId: string, params?: { query?: string; page?: number; limit?: number }): Promise<PagedEntityResponse<CrmAppointment>> => {
  const response = await apiClient.get<PagedEntityResponse<CrmAppointment>>(API_ENDPOINTS.CRM.APPOINTMENTS(orgId), { params });
  return response.data;
};

export const createAppointment = async (orgId: string, data: any): Promise<CrmAppointment> => {
  const response = await apiClient.post<CrmAppointment>(API_ENDPOINTS.CRM.APPOINTMENTS(orgId), data);
  return response.data;
};

export interface SaleTeamResponse {
  id: string;
  name: string;
  isArchived: boolean;
  leader?: { id: string; firstName: string; lastName: string; email: string };
  members?: { id: string; firstName: string; lastName: string; email: string }[];
}

export const getSaleTeams = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number; isArchived?: boolean }
): Promise<PagedEntityResponse<SaleTeamResponse>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleTeamResponse>>(
    API_ENDPOINTS.CRM.SALE_TEAMS(orgId),
    { params }
  );
  return response.data;
};

export const getMySaleTeams = async (
  orgId: string
): Promise<SaleTeamResponse[]> => {
  const response = await apiClient.get<SaleTeamResponse[]>(
    `${API_ENDPOINTS.CRM.SALE_TEAMS(orgId)}/me`
  );
  return response.data;
};

export const getSaleTeamById = async (orgId: string, id: string): Promise<SaleTeamResponse> => {
  const response = await apiClient.get<SaleTeamResponse>(`${API_ENDPOINTS.CRM.SALE_TEAMS(orgId)}/${id}`);
  return response.data;
};

export const createSaleTeam = async (orgId: string, data: { name: string; leaderId: string; memberIds?: string[] }): Promise<SaleTeamResponse> => {
  const response = await apiClient.post<SaleTeamResponse>(API_ENDPOINTS.CRM.SALE_TEAMS(orgId), data);
  return response.data;
};

export const updateSaleTeam = async (orgId: string, id: string, data: { name: string; leaderId: string; memberIds?: string[] }): Promise<SaleTeamResponse> => {
  const response = await apiClient.put<SaleTeamResponse>(`${API_ENDPOINTS.CRM.SALE_TEAMS(orgId)}/${id}`, data);
  return response.data;
};

export const deleteSaleTeam = async (orgId: string, id: string): Promise<void> => {
  await apiClient.delete(`${API_ENDPOINTS.CRM.SALE_TEAMS(orgId)}/${id}`);
};

// ─── CRM ANALYTICS ───────────────────────────────────────────────────────────

export interface LeadStageCount {
  stage: string;
  count: number;
}

export interface PipelineStageSummary {
  stage: string;
  leadCount: number;
  totalExpectedRevenue: number;
  weightedRevenue: number;
  averageProbability: number;
}

export const getLeadStageFunnel = async (
  orgId: string
): Promise<LeadStageCount[]> => {
  const response = await apiClient.get<LeadStageCount[]>(
    API_ENDPOINTS.ANALYTICS.PIPELINE_LEAD_FUNNEL(orgId)
  );
  return response.data;
};

export const getPipelineSummary = async (
  orgId: string
): Promise<PipelineStageSummary[]> => {
  const response = await apiClient.get<PipelineStageSummary[]>(
    API_ENDPOINTS.ANALYTICS.PIPELINE_SUMMARY(orgId)
  );
  return response.data;
};

