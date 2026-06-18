'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useToast } from '@/hooks/useToast';
import { useAuthStore } from '@/store/use-auth-store';
import { getUserPermissions } from '@/services/mockPermissions';
import { toast } from 'sonner';
import { APP_ROUTES } from '@/config/constants';

interface UseSelectOrgReturn {
  loading: boolean;
  error: string | null;
  handleSelectOrg: (orgId: string) => Promise<void>;
}

/**
 * useSelectOrg Hook
 * 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (ORG SELECTION - DÀNH CHO ORG USER)
 * 
 * Quy trình:
 * 1. Tại /onboarding/select-org, ứng dụng gọi API lấy danh sách các Org mà User tham gia
 * 2. User chọn 1 Org:
 *    - Cập nhật currentOrgId vào Zustand Store (state management)
 *    - Lưu currentOrgId vào Cookie (để Middleware Server-side có thể đọc)
 *    - Load permissions của user tại org này
 *    - Điều hướng vào Dashboard chính: /(dashboard)/[orgId]
 * 
 * Note: System Admin sẽ bypass bước này vì sẽ thẳng vào /administration
 */
export const useSelectOrg = (): UseSelectOrgReturn => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { user, setCurrentOrgId, setPermissions } = useAuthStore();
  const { toastError } = useToast();
  const router = useRouter();

  const handleSelectOrg = async (orgId: string) => {
    setLoading(true);
    setError(null);

    try {
      // 🔵 BƯỚC 3.1: Validate orgId (check xem user có quyền access org này không)
      // Thường backend sẽ validate, ở đây check client-side
      
      if (!orgId) {
        throw new Error('Invalid organization');
      }

      // 🔵 BƯỚC 3.2: Cập nhật currentOrgId vào Zustand Store
      setCurrentOrgId(orgId);

      // 🔵 BƯỚC 3.3: Lưu currentOrgId vào Cookie
      // Middleware Server-side sẽ đọc cookie này để validate requests
      document.cookie = `currentOrgId=${orgId}; path=/; max-age=86400; secure; samesite=strict`;

      // 🔵 BƯỚC 3.4: Load permissions của user tại org này
      // Có thể gọi backend API để lấy permissions org-specific
      // Tạm thời dùng mock data từ user permissions
      if (user?.id) {
        const userPermissions = await getUserPermissions(user.id);
        // Filter permissions to org-specific (có thể backend sẽ xử lý)
        setPermissions(userPermissions);
      }

      // 🔵 BƯỚC 3.5: Điều hướng vào Dashboard chính
      // Sử dụng dynamic route /(dashboard)/[orgId]
      const dashboardPath = APP_ROUTES.DASHBOARD(orgId);
      
      toast.success(`Switched to organization: ${orgId}`);
      router.push(dashboardPath);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to select organization';
      setError(errorMessage);
      toastError(err, 'Organization Selection Failed');
    } finally {
      setLoading(false);
    }
  };

  return { loading, error, handleSelectOrg };
};
