'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useToast } from '@/hooks/useToast';
import { useAuthStore } from '@/store/use-auth-store';
import { login } from '../services/authService';
import { getUserPermissions } from '@/services/mockPermissions';
import { mockUserOrganizations } from '@/services/mockOrganizations';
import { getRedirectPath } from '@/services/authFlow';
import { toast } from 'sonner';
import type { User } from '@/types/user';

interface UseLoginReturn {
  loading: boolean;
  error: string | null;
  handleLogin: (email: string, password: string) => Promise<void>;
}

/**
 * useLogin Hook
 * 🟢 BƯỚC 1: XÁC THỰC (AUTHENTICATION)
 * 
 * Hành động:
 * 1. Người dùng nhập Email/Password tại /login
 * 2. Backend xác thực thông tin (hoặc mock)
 * 3. Backend gắn access_token và refresh_token vào HttpOnly Cookie
 * 4. Trả về JSON chứa thông tin User cơ bản (bao gồm flag isSystemAdmin: boolean)
 * 
 * Frontend xử lý:
 * - Lưu thông tin User vào Zustand Auth Store
 * - Load danh sách Organization
 * - Load danh sách Permission tương ứng
 * - 🟡 BƯỚC 2 thực hiện tại đây: Phân luồng điều hướng
 */
export const useLogin = (): UseLoginReturn => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { setUser, setOrganizations, setPermissions } = useAuthStore();
  const { toastError } = useToast();
  const router = useRouter();

  const handleLogin = async (email: string, password: string) => {
    setLoading(true);
    setError(null);

    try {
      // 🟢 BƯỚC 1.1: Gọi API xác thực
      // Backend xác thực và gắn cookies: access_token, refresh_token (HttpOnly)
      // await login({ email, password });

      // ⚠️ MOCK IMPLEMENTATION - Tạm thời sử dụng mock data
      // Trong production, phần này sẽ gọi backend API thực
      const mockUsers: Record<string, { user: User; password: string }> = {
        'admin@erp.com': {
          user: {
            id: 'user-3',
            firstName: 'System',
            lastName: 'Admin',
            email: 'admin@erp.com',
            role: 'system_admin',
            isSystemAdmin: true, // Flag quan trọng để phân luồng
            avatarUrl: '',
          },
          password: 'admin123',
        },
        'user@erp.com': {
          user: {
            id: 'user-1',
            firstName: 'John',
            lastName: 'Doe',
            email: 'user@erp.com',
            role: 'org_user',
            isSystemAdmin: false,
            avatarUrl: '',
          },
          password: 'user123',
        },
        'manager@erp.com': {
          user: {
            id: 'user-2',
            firstName: 'Jane',
            lastName: 'Manager',
            email: 'manager@erp.com',
            role: 'org_user',
            isSystemAdmin: false,
            avatarUrl: '',
          },
          password: 'manager123',
        },
      };

      const userData = mockUsers[email];
      if (!userData || userData.password !== password) {
        throw new Error('Invalid email or password');
      }

      const user = userData.user;

      // 🟢 BƯỚC 1.2: Lưu thông tin User vào Zustand Store
      setUser(user);

      // 🟢 BƯỚC 1.3: Load danh sách Organization mà User này tham gia
      // Thông thường backend sẽ trả về danh sách org, tạm dùng mock
      setOrganizations(mockUserOrganizations);

      // 🟢 BƯỚC 1.4: Load danh sách Permission của User
      const permissions = await getUserPermissions(user.id);
      setPermissions(permissions);

      // 🔵 BƯỚC 1.5: Set Cookies cho Middleware Server-side đọc
      // (Normally set by backend, nhưng ở đây mock cho frontend)
      
      // Set orgIds cookie - danh sách các org mà user tham gia
      const orgIds = mockUserOrganizations.map(org => org.id).join(',');
      document.cookie = `userOrgIds=${orgIds}; path=/; max-age=86400; secure; samesite=strict`;

      // Set user role cookie - để middleware có thể check
      document.cookie = `userRole=${user.role}; path=/; max-age=86400; secure; samesite=strict`;

      // Mock access token cookie (normally set by backend via Set-Cookie header)
      document.cookie = `access_token=mock-jwt-token-${user.id}; path=/; max-age=86400; secure; samesite=strict; httponly`;

      // System admin flag (tạm thời, thường không cần vì dùng role)
      document.cookie = `isSystemAdmin=${user.isSystemAdmin}; path=/; max-age=86400; secure; samesite=strict`;

      // 🟡 BƯỚC 2: PHÂN LUỒNG ĐIỀU HƯỚNG (ROUTING DECISION)
      const redirectPath = getRedirectPath(user);
      
      toast.success(`Welcome ${user.firstName}! Redirecting...`);
      
      // Redirect dựa trên vai trò
      router.push(redirectPath);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Login failed';
      setError(errorMessage);
      toastError(err, 'Sign in failed');
    } finally {
      setLoading(false);
    }
  };

  return { loading, error, handleLogin };
};
