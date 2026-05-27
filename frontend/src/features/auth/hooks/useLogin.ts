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
        // ═══════════════════════════════════════════════════════════
        // 🟢 BƯỚC 1: XÁC THỰC (AUTHENTICATION) QUA API THẬT
        // ═══════════════════════════════════════════════════════════
        // Axios interceptor đã được cấu hình gửi/nhận cookie (withCredentials: true)
        const user = await loginApi({
          email: values.email,
          password: values.password,
        });

        // ═══════════════════════════════════════════════════════════
        // 🟢 BƯỚC 2: LƯU STATE VÀO ZUSTAND STORE
        // ═══════════════════════════════════════════════════════════

        // Lưu thông tin user cơ bản
        setUser(user);

        // Lưu danh sách Organization trả về từ backend
        const userOrgs = user.organizations || [];
        setOrganizations(userOrgs);

        // Load & lưu danh sách Permission (RBAC)
        // TODO: Đọc permissions từ object user nếu backend trả về, hoặc gọi API
        const permissions = await getUserPermissions(user.id);
        setPermissions(permissions);

        // ═══════════════════════════════════════════════════════════
        // 🔵 BƯỚC 3: SET COOKIES CHO MIDDLEWARE 
        // ═══════════════════════════════════════════════════════════
        // access_token và refresh_token đã được BE set qua HttpOnly.
        // Frontend chỉ cần set userOrgIds để middleware Next.js validate định tuyến.
        const orgIds = userOrgs.map((org) => org.id).join(',');
        document.cookie = `userOrgIds=${orgIds}; path=/; max-age=86400; samesite=strict`;

        // ═══════════════════════════════════════════════════════════
        // 🟡 BƯỚC 4: PHÂN LUỒNG ĐIỀU HƯỚNG
        // ═══════════════════════════════════════════════════════════

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const redirectPath = getRedirectPath(user as any);

        toast.success(`Chào mừng, ${user.firstName}! Đang chuyển hướng...`, {
          duration: 2000,
        });

        router.push(redirectPath);
      } catch {
        // Lỗi đã được Global Axios Interceptor xử lý và show Toast!
        // Ở đây chỉ setServerError để form có thể hiển thị inline (nếu muốn)
        setServerError('Đăng nhập thất bại. Vui lòng kiểm tra lại.');
      } finally {
        setLoading(false);
      }
    },
    [setUser, setOrganizations, setPermissions, router]
  );

  return { loading, serverError, handleLogin };
};
