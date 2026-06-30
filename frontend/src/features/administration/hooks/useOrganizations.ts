// src/features/administration/hooks/useOrganizations.ts
// Hook for managing organizations - CRUD operations
// Migration friendly: Replace with real API calls when backend is ready

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { mockOrganizations } from '../mock-data';
import type { Organization, OrganizationFormData } from '../types';

export const useOrganizations = () => {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(false);

  // Simulate API fetch
  useEffect(() => {
    const fetchOrganizations = async () => {
      setLoading(true);
      try {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500));
        setOrganizations(mockOrganizations);
      } catch (error) {
        console.error('Failed to fetch organizations:', error);
        toast.error('Failed to load organizations');
      } finally {
        setLoading(false);
      }
    };

    fetchOrganizations();
  }, []);

  const createOrganization = async (data: OrganizationFormData): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      const newOrg: Organization = {
        id: `org-${Date.now()}`,
        ...data,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setOrganizations(prev => [...prev, newOrg]);
      toast.success('Organization created successfully');
    } catch (error) {
      console.error('Failed to create organization:', error);
      toast.error('Failed to create organization');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const updateOrganization = async (id: string, data: OrganizationFormData): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setOrganizations(prev =>
        prev.map(org =>
          org.id === id
            ? { ...org, ...data, updatedAt: new Date().toISOString() }
            : org
        )
      );
      toast.success('Organization updated successfully');
    } catch (error) {
      console.error('Failed to update organization:', error);
      toast.error('Failed to update organization');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const deleteOrganization = async (id: string): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500));

      setOrganizations(prev => prev.filter(org => org.id !== id));
      toast.success('Organization deleted successfully');
    } catch (error) {
      console.error('Failed to delete organization:', error);
      toast.error('Failed to delete organization');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return {
    organizations,
    loading,
    createOrganization,
    updateOrganization,
    deleteOrganization,
  };
};
