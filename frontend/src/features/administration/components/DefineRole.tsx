// src/features/administration/components/DefineRole.tsx
// Define Role Component - Create/Edit role with permission matrix
// Follows DESIGN.md: Form pattern, modal, proper spacing

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { RoleMatrix } from './RoleMatrix';
import type { Role, RoleFormData } from '../types';

interface DefineRoleProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: RoleFormData) => void;
  role?: Role | null;
  loading?: boolean;
}

interface RoleFormErrors {
  name?: string;
  permissions?: string;
}

export const DefineRole: React.FC<DefineRoleProps> = ({
  isOpen,
  onClose,
  onSubmit,
  role,
  loading = false,
}) => {
  const initialFormData = React.useMemo(() => ({
    name: role?.name || '',
    permissions: role?.permissions || [],
  }), [role]);

  const [formData, setFormData] = useState<RoleFormData>(initialFormData);

  const [errors, setErrors] = useState<RoleFormErrors>({});

  const validateForm = (): boolean => {
    const newErrors: RoleFormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Tên vai trò là bắt buộc';
    } else if (formData.name.length < 2) {
      newErrors.name = 'Tên vai trò phải có ít nhất 2 ký tự';
    }

    if (formData.permissions.length === 0) {
      newErrors.permissions = 'Phải chọn ít nhất một quyền';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
    }
  };

  const handlePermissionsChange = (permissions: string[]) => {
    setFormData(prev => ({ ...prev, permissions }));
    if (errors.permissions) {
      setErrors(prev => ({ ...prev, permissions: undefined }));
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-6xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-charcoal">
            {role ? 'Chỉnh sửa Vai trò' : 'Định nghĩa Vai trò Mới'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <Card className="shadow-standard">
            <CardHeader>
              <CardTitle className="text-lg font-semibold text-charcoal">
                Thông tin Cơ bản
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="roleName" className="text-sm font-semibold text-charcoal">
                    Tên Vai trò *
                  </Label>
                  <Input
                    id="roleName"
                    value={formData.name}
                    onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                    placeholder="Ví dụ: Quản lý kho, Nhân viên bán hàng"
                    className={errors.name ? 'border-error-red' : ''}
                  />
                  {errors.name && (
                    <p className="text-sm text-error-red">{errors.name}</p>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          <RoleMatrix
            selectedPermissions={formData.permissions}
            onPermissionsChange={handlePermissionsChange}
          />

          {errors.permissions && (
            <div className="p-4 bg-error-red/10 border border-error-red rounded-md">
              <p className="text-sm text-error-red">{errors.permissions}</p>
            </div>
          )}

          <DialogFooter className="gap-3">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={loading}
            >
              Hủy
            </Button>
            <Button
              type="submit"
              disabled={loading}
              className="bg-primary-blue hover:bg-dark-blue text-white"
            >
              {loading ? 'Đang lưu...' : role ? 'Cập nhật' : 'Tạo Vai trò'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
