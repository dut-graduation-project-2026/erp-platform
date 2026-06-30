/**
 * PermissionGuard
 *
 * Wrapper component that conditionally renders children based on the user's
 * real permissions (fetched from the backend and stored in the auth store).
 *
 * Usage:
 *   <PermissionGuard permission="partners:create">
 *     <Button>New Customer</Button>
 *   </PermissionGuard>
 *
 *   <PermissionGuard permission="orders:write" fallback={<span>Read-only</span>}>
 *     <EditForm />
 *   </PermissionGuard>
 */

import React, { useState, useEffect } from 'react';
import { usePermissions } from '@/hooks/use-permissions';
import { useAuthStore } from '@/store/use-auth-store';

interface PermissionGuardProps {
  permission: string;
  fallback?: React.ReactNode;
  children: React.ReactNode;
}

export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  permission,
  fallback = null,
  children,
}) => {
  const [mounted, setMounted] = useState(false);
  const { hasPermission } = usePermissions();
  const { user } = useAuthStore();

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return null;
  }

  if (!user) {
    return null;
  }

  if (!hasPermission(permission)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};
