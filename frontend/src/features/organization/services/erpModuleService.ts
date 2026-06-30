import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';

export interface ErpModule {
  id: string;
  name: string;
  code: string;
  description: string;
}

export interface Permission {
  id: string;
  name: string;
  description: string;
  code: string;
}

export interface ErpModuleWithPermissions extends ErpModule {
  permissions: Permission[];
}

export const fetchMyModulesApi = async (organizationId: string): Promise<ErpModule[]> => {
  const response = await apiClient.get<ErpModule[]>(API_ENDPOINTS.ERP_MODULES.ME, {
    params: { organizationId }
  });
  return response.data;
};

export const fetchAllModulesApi = async (organizationId: string): Promise<ErpModuleWithPermissions[]> => {
  const response = await apiClient.get<ErpModuleWithPermissions[]>(API_ENDPOINTS.ERP_MODULES.BASE, {
    params: { organizationId }
  });
  return response.data;
};
