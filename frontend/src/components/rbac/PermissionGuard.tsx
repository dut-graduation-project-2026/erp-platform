import { ReactNode } from "react";
import { usePermissions } from "@/hooks/use-permissions";

interface PermissionGuardProps {
  permissionCode: string;
  children: ReactNode;
  fallback?: ReactNode;
}

export const PermissionGuard = ({
  permissionCode,
  children,
  fallback = null,
}: PermissionGuardProps) => {
  const { hasPermission } = usePermissions();

  if (!hasPermission(permissionCode)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};
