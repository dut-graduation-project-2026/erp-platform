import { useState, useEffect, useCallback } from 'react';
import { fetchUsersApi, UserBaseResponse, FetchUsersParams } from '../services/userService';

export const useUsers = (initialParams: FetchUsersParams) => {
  const [users, setUsers] = useState<UserBaseResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [params, setParams] = useState<FetchUsersParams>(initialParams);

  const loadUsers = useCallback(async () => {
    if (!params.organizationId) return;
    
    try {
      setLoading(true);
      const res = await fetchUsersApi(params);
      setUsers(res.data || []);
      setTotalElements(res.totalElements || 0);
      setTotalPages(res.totalPages || 0);
      setError(null);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setError(err.message || 'Failed to fetch users');
    } finally {
      setLoading(false);
    }
  }, [params]);

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  const refresh = () => {
    loadUsers();
  };

  return { 
    users, 
    totalElements, 
    totalPages, 
    loading, 
    error, 
    params, 
    setParams,
    refresh 
  };
};
