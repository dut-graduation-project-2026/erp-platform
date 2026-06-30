import { useAuthStore } from "@/store/use-auth-store";
import { MODULE_PERMISSIONS_MAP } from "@/config/permissions";

/**
 * usePermissions
 *
 * Reads the flat `permissions` string-array stored in the auth store.
 * Permissions are populated in `select-org/page.tsx` via fetchMyPermissionsApi()
 * and stored with `setPermissions()` — they live at the TOP LEVEL of the store,
 * NOT nested inside each organization object.
 */
export const usePermissions = () => {
  // ✅ Read from top-level `permissions` — NOT from org.permissions (which is undefined)
  const { permissions } = useAuthStore();

  const checkSinglePermission = (permCode: string): boolean => {
    if (permissions.includes(permCode)) {
      return true;
    }

    const parts = permCode.split(':');
    if (parts.length !== 2) return false;
    const [module, action] = parts;

    if (action === 'read' || action === 'select') {
      return permissions.includes(`${module}:read_all`) || 
             permissions.includes(`${module}:write_all`);
    }
    if (action === 'write' || action === 'create' || action === 'delete') {
      return permissions.includes(`${module}:write_all`);
    }
    return false;
  };

  const hasPermission = (permissionCode: string): boolean => {
    return checkSinglePermission(permissionCode);
  };

  const hasAnyPermission = (permissionCodes: string[]): boolean => {
    return permissionCodes.some((code) => checkSinglePermission(code));
  };

  const hasAllPermissions = (permissionCodes: string[]): boolean => {
    return permissionCodes.every((code) => checkSinglePermission(code));
  };

  /**
   * Check if a user has access to a specific module on the launcher
   */
  const hasModuleAccess = (moduleId: string, defaultPermission?: string): boolean => {
    const resources = MODULE_PERMISSIONS_MAP[moduleId];
    if (resources) {
      return permissions.some((perm) => {
        const [permResource] = perm.split(':');
        return resources.includes(permResource);
      });
    }
    // Fallback to checking the default permission string if specified
    if (defaultPermission) {
      return checkSinglePermission(defaultPermission);
    }
    return false;
  };

  return {
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasModuleAccess,
  };
};
