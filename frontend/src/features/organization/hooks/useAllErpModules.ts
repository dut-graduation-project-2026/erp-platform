import { useState, useCallback, useEffect } from 'react';
import { ErpModuleWithPermissions, fetchAllModulesApi } from '../services/erpModuleService';

export const useAllErpModules = (organizationId: string | null) => {
  const [modules, setModules] = useState<ErpModuleWithPermissions[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchModules = useCallback(async () => {
    if (!organizationId) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await fetchAllModulesApi(organizationId);
      setModules(data || []);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error('Fetch all modules error:', err);
      setError(err.response?.data?.message || 'Failed to fetch modules.');
    } finally {
      setLoading(false);
    }
  }, [organizationId]);

  useEffect(() => {
    fetchModules();
  }, [fetchModules]);

  return {
    modules,
    loading,
    error,
    refreshModules: fetchModules
  };
};
