'use client';

/**
 * @file useRegister.ts
 * @description Custom Hook xử lý toàn bộ nghiệp vụ đăng ký tài khoản.
 */

import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

import { useAuthStore } from '@/store/use-auth-store';
import { getRedirectPath } from '@/services/authFlow';

// TODO: Thay bằng API get permissions thật từ backend khi backend hỗ trợ
import { getUserPermissions } from '@/services/mockPermissions';

import { registerApi } from '@/features/auth/services/authService';
import type { RegisterFormValues, UseRegisterReturn } from '@/features/auth/types/auth.types';

export const useRegister = (): UseRegisterReturn => {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const { setUser, setOrganizations, setPermissions } = useAuthStore();
  const router = useRouter();

  const handleRegister = useCallback(
    async (values: RegisterFormValues) => {
      setLoading(true);
      setServerError(null);

      try {
        const user = await registerApi({
          email: values.email,
          password: values.password,
          firstName: values.firstName,
          lastName: values.lastName,
        });
        setUser(user);

        // Lưu danh sách Organization trả về từ backend
        const userOrgs = user.organizations || [];
        setOrganizations(userOrgs);
        const permissions = await getUserPermissions(user.id);
        setPermissions(permissions);

        const orgIds = userOrgs.map((org) => org.id).join('_');
        document.cookie = `userOrgIds=${orgIds}; path=/; max-age=86400; secure; samesite=strict`;
        document.cookie = `clientSession=true; path=/; max-age=86400; secure; samesite=strict`;
        const searchParams = new URLSearchParams(window.location.search);
        const redirectParam = searchParams.get('redirect');
        const redirectPath = redirectParam || getRedirectPath(user as any);

        toast.success(`Đăng ký tài khoản thành công! Chào mừng, ${user.firstName}!`, {
          duration: 2000,
        });

        router.push(redirectPath);
      } catch (err: any) {
        const errMsg = err?.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.';
        setServerError(errMsg);
      } finally {
        setLoading(false);
      }
    },
    [setUser, setOrganizations, setPermissions, router]
  );

  return { loading, serverError, handleRegister };
};
