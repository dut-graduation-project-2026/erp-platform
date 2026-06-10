// src/features/administration/hooks/useRoles.ts
// Hook for managing roles - CRUD operations
// Migration friendly: Replace with real API calls when backend is ready

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { mockRoles } from '../mock-data';
import type { Role, RoleFormData } from '../types';

export const useRoles = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(false);

  // Simulate API fetch
  useEffect(() => {
    const fetchRoles = async () => {
      setLoading(true);
      try {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500));
        setRoles(mockRoles);
      } catch (error) {
        console.error('Failed to fetch roles:', error);
        toast.error('Không thể tải danh sách vai trò');
      } finally {
        setLoading(false);
      }
    };

    fetchRoles();
  }, []);

  const createRole = async (data: RoleFormData & { organizationId: string }): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      const newRole: Role = {
        id: `role-${Date.now()}`,
        name: data.name,
        permissions: data.permissions,
        organizationId: data.organizationId,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setRoles(prev => [...prev, newRole]);
      toast.success('Tạo vai trò thành công');
    } catch (error) {
      console.error('Failed to create role:', error);
      toast.error('Không thể tạo vai trò');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const updateRole = async (id: string, data: RoleFormData): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setRoles(prev =>
        prev.map(role =>
          role.id === id
            ? { ...role, ...data, updatedAt: new Date().toISOString() }
            : role
        )
      );
      toast.success('Cập nhật vai trò thành công');
    } catch (error) {
      console.error('Failed to update role:', error);
      toast.error('Không thể cập nhật vai trò');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const deleteRole = async (id: string): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500));

      setRoles(prev => prev.filter(role => role.id !== id));
      toast.success('Xóa vai trò thành công');
    } catch (error) {
      console.error('Failed to delete role:', error);
      toast.error('Không thể xóa vai trò');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return {
    roles,
    loading,
    createRole,
    updateRole,
    deleteRole,
  };
};
