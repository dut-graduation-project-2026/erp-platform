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

  const hasPermission = (permissionCode: string): boolean => {
    return permissions.includes(permissionCode);
  };

  const hasAnyPermission = (permissionCodes: string[]): boolean => {
    return permissionCodes.some((code) => permissions.includes(code));
  };

  const hasAllPermissions = (permissionCodes: string[]): boolean => {
    return permissionCodes.every((code) => permissions.includes(code));
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
      return permissions.includes(defaultPermission);
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
