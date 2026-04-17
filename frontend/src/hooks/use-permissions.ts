import { useAuthStore } from "@/store/use-auth-store";

export const usePermissions = () => {
  const { user, organizations, currentOrgId } = useAuthStore();

  const currentOrg = organizations.find(org => org.id === currentOrgId);

  const hasPermission = (permissionCode: string): boolean => {
    return currentOrg?.permissions?.includes(permissionCode) ?? false;
  };

  const hasAnyPermission = (permissionCodes: string[]): boolean => {
    return permissionCodes.some(code => hasPermission(code));
  };

  const hasAllPermissions = (permissionCodes: string[]): boolean => {
    return permissionCodes.every(code => hasPermission(code));
  };

  return {
    permissions: currentOrg?.permissions ?? [],
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
  };
};
