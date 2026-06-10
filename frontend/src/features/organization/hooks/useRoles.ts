import { useState, useEffect } from 'react';
import { fetchRolesApi, fetchRoleDetailApi, RoleBaseResponse, RoleResponse } from '../services/roleService';

export const useRoles = (orgId: string) => {
  const [roles, setRoles] = useState<RoleBaseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orgId) return;

    const loadRoles = async () => {
      try {
        setLoading(true);
        const res = await fetchRolesApi(orgId);
        setRoles(res.data || []);
        setError(null);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: unknown) {
        if (err instanceof Error) {
          setError(err.message);
        } else {
          setError('Failed to fetch roles');
        }
      } finally {
        setLoading(false);
      }
    };

    loadRoles();
  }, [orgId]);

  return { roles, loading, error };
};

export const useRoleDetail = (orgId: string, roleId: string) => {
  const [role, setRole] = useState<RoleResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orgId || !roleId || roleId === 'new') {
      setLoading(false);
      return;
    }

    const loadRole = async () => {
      try {
        setLoading(true);
        const res = await fetchRoleDetailApi(orgId, roleId);
        setRole(res);
        setError(null);
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        const status = err?.response?.status;
        if (status === 400) {
          setError('Role not found or does not belong to this organization.');
        } else if (status === 403) {
          setError('You do not have permission to view this role.');
        } else {
          setError(err?.response?.data?.message || 'Failed to fetch role details.');
        }
      } finally {
        setLoading(false);
      }
    };

    loadRole();
  }, [orgId, roleId]);

  return { role, loading, error };
};

