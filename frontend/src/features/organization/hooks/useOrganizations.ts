import { useState, useEffect, useCallback } from 'react';
import { useToast } from '@/hooks/useToast';
import { useAuthStore } from '@/store/use-auth-store';
import { fetchMyOrganizationsApi, createOrganizationApi, updateOrganizationApi, CreateOrganizationRequest, UpdateOrganizationRequest, OrganizationResponse } from '../services/organizationService';

export interface Organization extends OrganizationResponse {
  role: string; // Mocked for now until backend provides role in the response
  permissions: string[];
}

export function useOrganizations() {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { toastError, toastSuccess } = useToast();
  const setAuthOrgs = useAuthStore((state) => state.setOrganizations);

  const fetchOrganizations = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const orgs = await fetchMyOrganizationsApi();
      
      // Use the actual user roles provided per organization from backend
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const orgsWithRoles = orgs.map((org: any) => ({
        ...org,
        role: org.role || 'Member',
        permissions: org.permissions || [],
      }));

      setOrganizations(orgsWithRoles);
      setAuthOrgs(orgsWithRoles);
    } catch (err) {
      setError('Failed to load organizations');
      toastError(err, 'Failed to load organizations. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [toastError, setAuthOrgs]);

  useEffect(() => {
    fetchOrganizations();
  }, [fetchOrganizations]);

  const createOrganization = async (payload: CreateOrganizationRequest) => {
    try {
      setLoading(true);
      await createOrganizationApi(payload);
      toastSuccess('Tạo tổ chức thành công!');
      await fetchOrganizations(); // Reload the list
      return true;
    } catch {
      // Global axios interceptor will show the toast for validation/business errors
      return false;
    } finally {
      setLoading(false);
    }
  };

  const updateOrganization = async (orgId: string, payload: UpdateOrganizationRequest) => {
    try {
      setLoading(true);
      await updateOrganizationApi(orgId, payload);
      toastSuccess('Cập nhật tổ chức thành công!');
      await fetchOrganizations(); // Reload the list
      return true;
    } catch {
      return false;
    } finally {
      setLoading(false);
    }
  };

  return {
    organizations,
    loading,
    error,
    refetch: fetchOrganizations,
    createOrganization,
    updateOrganization
  };
}
