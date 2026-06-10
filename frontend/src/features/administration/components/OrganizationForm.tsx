// src/features/administration/components/OrganizationForm.tsx
// Organization Form Component - Add/Edit organization
// Follows DESIGN.md: Form pattern, Segoe UI, proper spacing

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Upload } from 'lucide-react';
import type { Organization, OrganizationFormData } from '../types';

interface OrganizationFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: OrganizationFormData) => void;
  organization?: Organization | null;
  loading?: boolean;
}

export const OrganizationForm: React.FC<OrganizationFormProps> = ({
  isOpen,
  onClose,
  onSubmit,
  organization,
  loading = false,
}) => {
  const initialFormData = React.useMemo(() => ({
    name: organization?.name || '',
    code: organization?.code || '',
    logo: organization?.logo || '',
    contactEmail: organization?.contactEmail || '',
    contactPhone: organization?.contactPhone || '',
    address: organization?.address || '',
  }), [organization]);

  const [formData, setFormData] = useState<OrganizationFormData>(initialFormData);

  const [errors, setErrors] = useState<Partial<OrganizationFormData>>({});

  const validateForm = (): boolean => {
    const newErrors: Partial<OrganizationFormData> = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Tên tổ chức là bắt buộc';
    }

    if (!formData.code.trim()) {
      newErrors.code = 'Mã code là bắt buộc';
    } else if (!/^[A-Z0-9]+$/.test(formData.code)) {
      newErrors.code = 'Mã code chỉ chứa chữ hoa và số';
    }

    if (formData.contactEmail && !/\S+@\S+\.\S+/.test(formData.contactEmail)) {
      newErrors.contactEmail = 'Email không hợp lệ';
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

  const handleInputChange = (field: keyof OrganizationFormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl font-semibold text-charcoal">
            {organization ? 'Chỉnh sửa Tổ chức' : 'Thêm Tổ chức Mới'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <Label htmlFor="name" className="text-sm font-semibold text-charcoal">
                Tên Tổ chức *
              </Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                placeholder="Nhập tên tổ chức"
                className={errors.name ? 'border-error-red' : ''}
              />
              {errors.name && (
                <p className="text-sm text-error-red">{errors.name}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="code" className="text-sm font-semibold text-charcoal">
                Mã Code *
              </Label>
              <Input
                id="code"
                value={formData.code}
                onChange={(e) => handleInputChange('code', e.target.value.toUpperCase())}
                placeholder="ABC"
                className={errors.code ? 'border-error-red' : ''}
              />
              {errors.code && (
                <p className="text-sm text-error-red">{errors.code}</p>
              )}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="logo" className="text-sm font-semibold text-charcoal">
              Logo (URL)
            </Label>
            <div className="flex gap-3">
              <Input
                id="logo"
                value={formData.logo}
                onChange={(e) => handleInputChange('logo', e.target.value)}
                placeholder="https://example.com/logo.png"
              />
              <Button type="button" variant="outline" size="sm">
                <Upload className="w-4 h-4" />
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <Label htmlFor="contactEmail" className="text-sm font-semibold text-charcoal">
                Email Liên hệ
              </Label>
              <Input
                id="contactEmail"
                type="email"
                value={formData.contactEmail}
                onChange={(e) => handleInputChange('contactEmail', e.target.value)}
                placeholder="contact@company.com"
                className={errors.contactEmail ? 'border-error-red' : ''}
              />
              {errors.contactEmail && (
                <p className="text-sm text-error-red">{errors.contactEmail}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="contactPhone" className="text-sm font-semibold text-charcoal">
                Số Điện thoại
              </Label>
              <Input
                id="contactPhone"
                value={formData.contactPhone}
                onChange={(e) => handleInputChange('contactPhone', e.target.value)}
                placeholder="+84 123 456 789"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="address" className="text-sm font-semibold text-charcoal">
              Địa chỉ
            </Label>
            <Textarea
              id="address"
              value={formData.address}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => handleInputChange('address', e.target.value)}
              placeholder="Nhập địa chỉ tổ chức"
              rows={3}
            />
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
              {loading ? 'Đang lưu...' : organization ? 'Cập nhật' : 'Thêm mới'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
