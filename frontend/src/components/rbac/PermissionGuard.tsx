// src/components/rbac/PermissionGuard.tsx
// Permission Guard Component - Protects UI based on user permissions
// Follows DESIGN.md: Uses Segoe UI, proper spacing, shadow levels

import React from 'react';
import { useAuthStore } from '@/store/use-auth-store';
import { hasPermission } from '@/services/mockPermissions';

interface PermissionGuardProps {
  permission: string; // e.g., 'sales:create'
  fallback?: React.ReactNode; // What to show if no permission
  children: React.ReactNode;
}

export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  permission,
  fallback = null,
  children,
}) => {
  const { permissions } = useAuthStore();

  const hasAccess = hasPermission(permissions, permission);

  if (!hasAccess) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};
