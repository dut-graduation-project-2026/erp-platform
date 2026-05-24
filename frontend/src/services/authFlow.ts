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
  avatarUrl?: string;
}

export const getRedirectPath = (user: AuthUser): string => {
  // Always go to select-org after login for this frontend
  // Note: (onboarding) is a route group in Next.js so it's omitted from the URL
  return '/select-org';
};
