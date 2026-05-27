// src/features/administration/components/tabs/SettingsTab.tsx
// Settings Tab - Organization settings and module configuration
// Includes organization details and module toggles

'use client';

import React, { useState } from 'react';
import { AlertCircle, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

import type { Organization, OrganizationFormData } from '../../types';

interface SettingsTabProps {
  organizationId: string;
  organization: Organization;
}

export const SettingsTab: React.FC<SettingsTabProps> = ({
  organization,
}) => {
  const [loading, setLoading] = useState(false);
  const [saved, setSaved] = useState(false);
  const [formData, setFormData] = useState<OrganizationFormData>({
    name: organization.name,
    code: organization.code,
    logo: organization.logo || '',
    contactEmail: organization.contactEmail || '',
    contactPhone: organization.contactPhone || '',
    address: organization.address || '',
  });

  const [moduleSettings, setModuleSettings] = useState({
    sales: true,
    inventory: true,
    finance: true,
    hr: true,
    blockchainAudit: false,
  });

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setSaved(false);
  };

  const handleModuleToggle = (module: keyof typeof moduleSettings) => {
    setModuleSettings(prev => ({
      ...prev,
      [module]: !prev[module],
    }));
    setSaved(false);
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      // TODO: Call API to save organization settings
      await new Promise(resolve => setTimeout(resolve, 1000));
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Organization Information */}
      <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
        <CardHeader className="border-b border-[#e0e0e0] pb-4">
          <CardTitle className="text-[18px] font-bold text-[#242424]">
            Thông tin Tổ chức
          </CardTitle>
        </CardHeader>

        <CardContent className="pt-6 space-y-4">
          {/* Organization Name */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <Label className="text-[14px] font-semibold text-[#242424]">
                Tên Tổ chức <span className="text-[#dc3545]">*</span>
              </Label>
              <Input
                value={formData.name}
                onChange={(e) => handleChange('name', e.target.value)}
                className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
                required
              />
            </div>

            {/* Organization Code */}
            <div className="space-y-2">
              <Label className="text-[14px] font-semibold text-[#242424]">
                Mã Code <span className="text-[#dc3545]">*</span>
              </Label>
              <Input
                value={formData.code}
                onChange={(e) => handleChange('code', e.target.value)}
                className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
                required
                disabled
              />
              <p className="text-[12px] text-[#898989]">Mã code không thể thay đổi</p>
            </div>
          </div>

          {/* Contact Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <Label className="text-[14px] font-semibold text-[#242424]">
                Email Liên hệ
              </Label>
              <Input
                type="email"
                value={formData.contactEmail}
                onChange={(e) => handleChange('contactEmail', e.target.value)}
                placeholder="contact@organization.com"
                className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-[14px] font-semibold text-[#242424]">
                Số điện thoại
              </Label>
              <Input
                value={formData.contactPhone}
                onChange={(e) => handleChange('contactPhone', e.target.value)}
                placeholder="+84 XX XXXX XXXX"
                className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              />
            </div>
          </div>

          {/* Address */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Địa chỉ
            </Label>
            <Textarea
              value={formData.address}
              onChange={(e) => handleChange('address', e.target.value)}
              placeholder="Nhập địa chỉ tổ chức"
              className="h-24 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
            />
          </div>

          {/* Save Button */}
          <div className="flex items-center gap-3 pt-4 border-t border-[#e0e0e0]">
            <Button
              onClick={handleSave}
              disabled={loading}
              className="bg-[#0066cc] hover:bg-[#004499] text-white font-semibold h-10 px-6 rounded-[4px]"
            >
              {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
            </Button>

            {saved && (
              <div className="flex items-center gap-2 text-green-700">
                <CheckCircle2 className="w-4 h-4" />
                <span className="text-[13px] font-500">Đã lưu thành công</span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Module Configuration */}
      <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
        <CardHeader className="border-b border-[#e0e0e0] pb-4">
          <CardTitle className="text-[18px] font-bold text-[#242424]">
            Cấu hình Mô-đun
          </CardTitle>
          <p className="text-[13px] text-[#898989] mt-2">
            Bật hoặc tắt các mô-đun chức năng cho tổ chức này
          </p>
        </CardHeader>

        <CardContent className="pt-6 space-y-4">
          {/* Module List */}
          <div className="space-y-3">
            {[
              { key: 'sales', label: 'Bán hàng', description: 'Quản lý đơn hàng, khách hàng, báo giá' },
              { key: 'inventory', label: 'Kho & Chuỗi cung ứng', description: 'Quản lý tồn kho, nhập xuất hàng' },
              { key: 'finance', label: 'Tài chính', description: 'Quản lý hóa đơn, thu chi, kế toán' },
              { key: 'hr', label: 'Nhân sự', description: 'Quản lý nhân viên, lương, tuyển dụng' },
              { key: 'blockchainAudit', label: 'Kiểm toán Blockchain', description: 'Ghi nhận giao dịch trên blockchain' },
            ].map(module => (
              <div
                key={module.key}
                className="flex items-center justify-between p-4 rounded-[4px] border border-[#e0e0e0] hover:bg-[#f8f8f8] transition-colors"
              >
                <div>
                  <h4 className="text-[14px] font-semibold text-[#242424]">{module.label}</h4>
                  <p className="text-[13px] text-[#898989] mt-1">{module.description}</p>
                </div>

                <label className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={moduleSettings[module.key as keyof typeof moduleSettings]}
                    onChange={() => handleModuleToggle(module.key as keyof typeof moduleSettings)}
                    className="w-5 h-5 rounded border-[#0066cc] cursor-pointer"
                  />
                </label>
              </div>
            ))}
          </div>

          {/* Save Button */}
          <div className="flex items-center gap-3 pt-4 border-t border-[#e0e0e0]">
            <Button
              onClick={handleSave}
              disabled={loading}
              className="bg-[#0066cc] hover:bg-[#004499] text-white font-semibold h-10 px-6 rounded-[4px]"
            >
              {loading ? 'Đang lưu...' : 'Lưu cấu hình'}
            </Button>

            {saved && (
              <div className="flex items-center gap-2 text-green-700">
                <CheckCircle2 className="w-4 h-4" />
                <span className="text-[13px] font-500">Đã lưu thành công</span>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Danger Zone */}
      <Card className="border border-[#dc3545] bg-[#fff5f5] shadow-[0px_1px_3px_rgba(220,53,69,0.12)]">
        <CardHeader className="border-b border-[#dc3545] pb-4">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-5 h-5 text-[#dc3545]" />
            <CardTitle className="text-[18px] font-bold text-[#dc3545]">
              Vùng Nguy hiểm
            </CardTitle>
          </div>
        </CardHeader>

        <CardContent className="pt-6 space-y-4">
          <p className="text-[13px] text-[#8b3a3a]">
            Các hành động dưới đây có thể gây ra hậu quả nghiêm trọng
          </p>

          <Button
            variant="outline"
            className="border-[#dc3545] text-[#dc3545] hover:bg-[#ffe6e6] h-10 px-4 rounded-[4px]"
          >
            Xóa Tổ chức
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};
