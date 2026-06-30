'use client';

/**
 * @file useLogin.ts
 * @description Custom Hook xử lý toàn bộ nghiệp vụ đăng nhập.
 * Áp dụng Single Responsibility Principle: Hook chỉ xử lý logic,
 * KHÔNG chứa bất kỳ JSX nào.
 *
 * Luồng xử lý:
 * 1. Gọi API xác thực (loginApi)
 * 2. Backend set HttpOnly Cookie (access_token, refresh_token)
 * 3. Lưu thông tin User vào Zustand Store
 * 4. Load danh sách Organization & Permissions
 * 5. Phân luồng điều hướng
 */

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

import { useAuthStore } from '@/store/use-auth-store';
import { getRedirectPath } from '@/services/authFlow';

// TODO: Thay bằng API get permissions thật từ backend khi backend hỗ trợ
import { getUserPermissions } from '@/services/mockPermissions';

import { loginApi } from '@/features/auth/services/authService';
import type { LoginFormValues, UseLoginReturn } from '@/features/auth/types/auth.types';

export const useLogin = (): UseLoginReturn => {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const { setUser, setOrganizations, setPermissions } = useAuthStore();
  const router = useRouter();

  const handleLogin = useCallback(
    async (values: LoginFormValues) => {
      setLoading(true);
      setServerError(null);

      try {
      
        // Axios interceptor đã được cấu hình gửi/nhận cookie (withCredentials: true)
        const user = await loginApi({
          email: values.email,
          password: values.password,
        });

        setUser(user);
        const userOrgs = user.organizations || [];
        setOrganizations(userOrgs);
        const permissions = await getUserPermissions(user.id);
        setPermissions(permissions);
        const orgIds = userOrgs.map((org) => org.id).join('_');
        const maxAge = values.rememberMe ? 2592000 : 86400; // 30 days vs 1 day
        document.cookie = `userOrgIds=${orgIds}; path=/; max-age=${maxAge}; secure; samesite=strict`;
        document.cookie = `clientSession=true; path=/; max-age=${maxAge}; secure; samesite=strict`;
        const searchParams = new URLSearchParams(window.location.search);
        const redirectParam = searchParams.get('redirect');
        const redirectPath = (redirectParam && redirectParam !== '/') ? redirectParam : getRedirectPath(user as any);

        toast.success(`Welcome back, ${user.firstName}! Redirecting...`, {
          duration: 2000,
        });

        router.push(redirectPath);
      } catch {
        setServerError('Login failed. Please check your credentials.');
      } finally {
        setLoading(false);
      }
    },
    [setUser, setOrganizations, setPermissions, router]
  );

  return { loading, serverError, handleLogin };
};
