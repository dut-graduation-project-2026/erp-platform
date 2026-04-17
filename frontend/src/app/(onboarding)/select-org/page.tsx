'use client';

import SelectOrgForm from '@/features/auth/components/select-org/SelectOrgForm';
import { useSelectOrg } from '@/features/auth/hooks/useSelectOrg';
import type { SelectOrgFormValues } from '@/features/auth/components/select-org/SelectOrgForm';

/**
 * SelectOrgPage
 * 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (PAGE)
 * 
 * Quy trình:
 * - Tại đây người dùng (org_user) lựa chọn 1 tổ chức từ danh sách
 * - Hệ thống sẽ load permissions, set cookies, redirect đến dashboard
 */
export default function SelectOrgPage() {
  const { loading, handleSelectOrg } = useSelectOrg();

  const handleSubmit = async (values: SelectOrgFormValues) => {
    await handleSelectOrg(values.orgId);
  };

  return <SelectOrgForm onSubmit={handleSubmit} isSubmitting={loading} />;
}
