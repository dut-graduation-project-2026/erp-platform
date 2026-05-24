import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';

export interface ErpModule {
  id: string;
  name: string;
  code: string;
  description: string;
}

export const fetchMyModulesApi = async (organizationId: string): Promise<ErpModule[]> => {
  const response = await apiClient.get<ErpModule[]>(API_ENDPOINTS.ERP_MODULES.ME, {
    params: { organizationId }
  });
  return response.data;
};
