// src/services/authFlow.ts
// Service for handling authentication flow logic
// Implements Step-by-Step Login Flow:
// 🟡 BƯỚC 2: PHÂN LUỒNG ĐIỀU HƯỚNG (ROUTING DECISION)
// Quyết định điểm đến dựa trên vai trò người dùng

export interface AuthUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string; // 'system_admin' | 'org_user'
  isSystemAdmin?: boolean;
  avatarUrl?: string;
}

/**
 * Determines redirect path based on user role
 * - System Admin → /administration/organizations (quản lý công ty & RBAC)
 * - Org User → /onboarding/select-org (chọn tổ chức để làm việc)
 * 
 * @param user - User object from login response
 * @returns Redirect destination path
 */
export const getRedirectPath = (user: AuthUser): string => {
  const isSystemAdmin = 
    user.role === 'system_admin' || 
    user.isSystemAdmin === true;

  if (isSystemAdmin) {
    // System Admin goes to organization management
    return '/administration/organizations';
  }

  // Regular org users go to select-org
  return '/onboarding/select-org';
};
