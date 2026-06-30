// src/features/administration/hooks/useUsers.ts
// Hook for managing users - CRUD operations
// Migration friendly: Replace with real API calls when backend is ready

import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { mockUsers } from '../mock-data';
import type { User, UserFormData } from '../types';

export const useUsers = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);

  // Simulate API fetch
  useEffect(() => {
    const fetchUsers = async () => {
      setLoading(true);
      try {
        // Simulate network delay
        await new Promise(resolve => setTimeout(resolve, 500));
        setUsers(mockUsers);
      } catch (error) {
        console.error('Failed to fetch users:', error);
        toast.error('Failed to load users');
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  const createUser = async (data: UserFormData & { organizationId: string }): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      const newUser: User = {
        id: `user-${Date.now()}`,
        email: data.email,
        firstName: data.firstName,
        lastName: data.lastName,
        roleId: data.roleId,
        organizationId: data.organizationId,
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      setUsers(prev => [...prev, newUser]);
      toast.success('User created successfully');
    } catch (error) {
      console.error('Failed to create user:', error);
      toast.error('Failed to create user');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const updateUser = async (id: string, data: UserFormData): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setUsers(prev =>
        prev.map(user =>
          user.id === id
            ? { ...user, ...data, updatedAt: new Date().toISOString() }
            : user
        )
      );
      toast.success('User updated successfully');
    } catch (error) {
      console.error('Failed to update user:', error);
      toast.error('Failed to update user');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const deleteUser = async (id: string): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500));

      setUsers(prev => prev.filter(user => user.id !== id));
      toast.success('User deleted successfully');
    } catch (error) {
      console.error('Failed to delete user:', error);
      toast.error('Failed to delete user');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const toggleUserStatus = async (id: string): Promise<void> => {
    setLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 500));

      setUsers(prev =>
        prev.map(user =>
          user.id === id
            ? { ...user, isActive: !user.isActive, updatedAt: new Date().toISOString() }
            : user
        )
      );
      toast.success('Status updated successfully');
    } catch (error) {
      console.error('Failed to toggle user status:', error);
      toast.error('Failed to update status');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  return {
    users,
    loading,
    createUser,
    updateUser,
    deleteUser,
    toggleUserStatus,
  };
};
