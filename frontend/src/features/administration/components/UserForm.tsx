// src/features/administration/components/UserForm.tsx
// User Form Component - Add/Edit user with role selection
// Follows DESIGN.md: Form pattern, modal, proper spacing

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Plus } from 'lucide-react';
import { DefineRole } from './DefineRole';
import type { User, Role, UserFormData, RoleFormData } from '../types';

interface UserFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: UserFormData) => void;
  onRoleCreated?: (role: Role) => void;
  user?: User | null;
  roles: Role[];
  loading?: boolean;
}

export const UserForm: React.FC<UserFormProps> = ({
  isOpen,
  onClose,
  onSubmit,
  onRoleCreated,
  user,
  roles,
  loading = false,
}) => {
  const initialFormData = React.useMemo(() => ({
    email: user?.email || '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    roleId: user?.roleId || '',
  }), [user]);

  const [formData, setFormData] = useState<UserFormData>(initialFormData);

  const [errors, setErrors] = useState<Partial<UserFormData>>({});

  const [quickDefineRoleOpen, setQuickDefineRoleOpen] = useState(false);

  const validateForm = (): boolean => {
    const newErrors: Partial<UserFormData> = {};

    if (!formData.email.trim()) {
      newErrors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
    }

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Họ là bắt buộc';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Tên là bắt buộc';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit({
        ...formData,
        roleId: formData.roleId || undefined,
      });
    }
  };

  const handleRoleCreated = (roleData: RoleFormData) => {
    // This would be handled by parent component
    // For now, just close the modal
    setQuickDefineRoleOpen(false);
    if (onRoleCreated) {
      // Mock role object
      const newRole: Role = {
        id: `role-${Date.now()}`,
        name: roleData.name,
        permissions: roleData.permissions,
        organizationId: 'org-1', // Default
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      onRoleCreated(newRole);
    }
  };

  const handleInputChange = (field: keyof UserFormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <>
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold text-charcoal">
              {user ? 'Chỉnh sửa Người dùng' : 'Thêm Người dùng Mới'}
            </DialogTitle>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <Label htmlFor="firstName" className="text-sm font-semibold text-charcoal">
                  Họ *
                </Label>
                <Input
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => handleInputChange('firstName', e.target.value)}
                  placeholder="Nguyễn"
                  className={errors.firstName ? 'border-error-red' : ''}
                />
                {errors.firstName && (
                  <p className="text-sm text-error-red">{errors.firstName}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="lastName" className="text-sm font-semibold text-charcoal">
                  Tên *
                </Label>
                <Input
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => handleInputChange('lastName', e.target.value)}
                  placeholder="Văn A"
                  className={errors.lastName ? 'border-error-red' : ''}
                />
                {errors.lastName && (
                  <p className="text-sm text-error-red">{errors.lastName}</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="email" className="text-sm font-semibold text-charcoal">
                Email *
              </Label>
              <Input
                id="email"
                type="email"
                value={formData.email}
                onChange={(e) => handleInputChange('email', e.target.value)}
                placeholder="nguyenvana@company.com"
                className={errors.email ? 'border-error-red' : ''}
              />
              {errors.email && (
                <p className="text-sm text-error-red">{errors.email}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="role" className="text-sm font-semibold text-charcoal">
                Vai trò
              </Label>
              <div className="flex gap-3">
                <Select
                  value={formData.roleId}
                  onValueChange={(value: string) => handleInputChange('roleId', value)}
                >
                  <SelectTrigger className="flex-1">
                    <SelectValue placeholder="Chọn vai trò (tùy chọn)" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">Không gán vai trò</SelectItem>
                    {roles.map((role) => (
                      <SelectItem key={role.id} value={role.id}>
                        {role.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setQuickDefineRoleOpen(true)}
                  className="px-3"
                  title="Tạo vai trò nhanh"
                >
                  <Plus className="w-4 h-4" />
                </Button>
              </div>
              <p className="text-xs text-mid-gray">
                Nhấn + để tạo vai trò mới ngay lập tức
              </p>
            </div>

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
                {loading ? 'Đang lưu...' : user ? 'Cập nhật' : 'Thêm mới'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <DefineRole
        isOpen={quickDefineRoleOpen}
        onClose={() => setQuickDefineRoleOpen(false)}
        onSubmit={handleRoleCreated}
      />
    </>
  );
};
