import { useState, useCallback, useEffect } from 'react';
import { ErpModule, fetchMyModulesApi } from '../services/erpModuleService';

export const useErpModules = (organizationId: string | null) => {
  const [modules, setModules] = useState<ErpModule[]>([]);
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
      const data = await fetchMyModulesApi(organizationId);
      setModules(data);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      console.error('Fetch modules error:', err);
      // Let global interceptor handle the toast, we just set the local error state
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
