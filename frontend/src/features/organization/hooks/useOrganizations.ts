import { useState, useEffect } from 'react';
import { apiClient } from '@/services/api-client';
import { useToast } from '@/hooks/useToast';

export interface Organization {
  id: string;
  name: string;
  description?: string;
  hotline?: string;
  address?: string;
  role: string; // Will be fetched from permissions API
}

export function useOrganizations() {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { toastError } = useToast();

  useEffect(() => {
    fetchOrganizations();
  }, []);

  const fetchOrganizations = async () => {
    try {
      setLoading(true);

      // Fetch organizations
      const orgResponse = await apiClient.get('/api/v1/organizations/me');
      const orgs = orgResponse.data;

      // Fetch permissions to get roles for each org
      // Note: Current API requires organizationId, so we'll mock roles for now
      // TODO: Create API to get all user permissions across organizations
      const orgsWithRoles = orgs.map((org: any, index: number) => ({
        ...org,
        role: index === 0 ? 'Admin' : index === 1 ? 'Manager' : 'Member', // Mock roles
      }));

      setOrganizations(orgsWithRoles);
    } catch (err) {
      setError('Failed to load organizations');
      toastError(err, 'Failed to load organizations. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return {
    organizations,
    loading,
    error,
    refetch: fetchOrganizations,
  };
}