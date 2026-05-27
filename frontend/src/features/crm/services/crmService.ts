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

export const getLeads = async (orgId: string, params?: { query?: string; page?: number; limit?: number }): Promise<PagedEntityResponse<CrmLead>> => {
  const response = await apiClient.get<PagedEntityResponse<CrmLead>>(API_ENDPOINTS.CRM.LEADS(orgId), { params });
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
