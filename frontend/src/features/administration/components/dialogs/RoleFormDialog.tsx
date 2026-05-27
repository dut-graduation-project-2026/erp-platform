// src/features/administration/components/dialogs/RoleFormDialog.tsx
// Role Form Dialog - Create/Edit role with permission matrix
// Includes interactive permission selection

'use client';

import React, { useState } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import type { Role, RoleFormData } from '../../types';

interface RoleFormDialogProps {
  role?: Role | null;
  organizationId: string;
  onClose: () => void;
  onSubmit: (data: RoleFormData) => Promise<void>;
}

const RESOURCES = [
  { name: 'Sales', label: 'Bán hàng' },
  { name: 'Inventory', label: 'Kho & Chuỗi cung ứng' },
  { name: 'Finance', label: 'Tài chính' },
  { name: 'HR', label: 'Nhân sự' },
  { name: 'Blockchain Audit', label: 'Kiểm toán Blockchain' },
];

const ACTIONS = ['VIEW', 'CREATE', 'EDIT', 'DELETE', 'EXPORT'];

export const RoleFormDialog: React.FC<RoleFormDialogProps> = ({
  role,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  organizationId,
  onClose,
  onSubmit,
}) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<RoleFormData>({
    name: role?.name || '',
    permissions: role?.permissions || [],
  });

  const handleChangeName = (value: string) => {
    setFormData(prev => ({ ...prev, name: value }));
  };

  const togglePermission = (resource: string, action: string) => {
    const permission = `${resource.toLowerCase()}:${action.toLowerCase()}`;
    setFormData(prev => ({
      ...prev,
      permissions: prev.permissions.includes(permission)
        ? prev.permissions.filter(p => p !== permission)
        : [...prev.permissions, permission],
    }));
  };

  const handleSelectAll = (resource: string) => {
    const resourcePermissions = ACTIONS.map(
      action => `${resource.toLowerCase()}:${action.toLowerCase()}`
    );
    const hasAll = resourcePermissions.every(p => formData.permissions.includes(p));

    setFormData(prev => ({
      ...prev,
      permissions: hasAll
        ? prev.permissions.filter(p => !resourcePermissions.includes(p))
        : [...new Set([...prev.permissions, ...resourcePermissions])],
    }));
  };

  const handleSelectAllActions = (action: string) => {
    const actionPermissions = RESOURCES.map(
      resource => `${resource.name.toLowerCase()}:${action.toLowerCase()}`
    );
    const hasAll = actionPermissions.every(p => formData.permissions.includes(p));

    setFormData(prev => ({
      ...prev,
      permissions: hasAll
        ? prev.permissions.filter(p => !actionPermissions.includes(p))
        : [...new Set([...prev.permissions, ...actionPermissions])],
    }));
  };

  const hasPermission = (resource: string, action: string) => {
    return formData.permissions.includes(`${resource.toLowerCase()}:${action.toLowerCase()}`);
  };

  const resourceHasAll = (resource: string) => {
    return ACTIONS.every(action => hasPermission(resource, action));
  };

  const actionHasAll = (action: string) => {
    return RESOURCES.every(resource => hasPermission(resource.name, action));
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await onSubmit(formData);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto border border-[#e0e0e0] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] rounded-[4px]">
        <DialogHeader className="border-b border-[#e0e0e0] pb-4 sticky top-0 bg-white">
          <DialogTitle className="text-[24px] font-bold text-[#242424]">
            {role ? 'Sửa Vai trò' : 'Tạo Vai trò'}
          </DialogTitle>
          <button
            onClick={onClose}
            className="absolute right-4 top-4 text-[#898989] hover:text-[#242424]"
          >
            <X className="w-5 h-5" />
          </button>
        </DialogHeader>

        <div className="space-y-6 py-6">
          {/* Role Name */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Tên Vai trò <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              value={formData.name}
              onChange={(e) => handleChangeName(e.target.value)}
              placeholder="VD: Quản lý Bán hàng"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
            />
          </div>

          {/* Permission Matrix */}
          <div className="space-y-4">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Ma trận Quyền
            </Label>

            <div className="overflow-x-auto border border-[#e0e0e0] rounded-[4px]">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                    <th className="px-4 py-3 text-left font-semibold text-[13px] text-[#242424] border-r border-[#e0e0e0]">
                      Tài nguyên
                    </th>
                    {ACTIONS.map(action => (
                      <th
                        key={action}
                        className="px-4 py-3 text-center font-semibold text-[13px] text-[#242424] border-r border-[#e0e0e0] cursor-pointer hover:bg-[#f0f0f0]"
                        onClick={() => handleSelectAllActions(action)}
                      >
                        <div className="flex flex-col items-center gap-1">
                          <span>{action}</span>
                          <Checkbox
                            checked={actionHasAll(action)}
                            className="w-4 h-4 rounded-[2px] border-[#0066cc]"
                          />
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {RESOURCES.map(resource => (
                    <tr key={resource.name} className="border-b border-[#e0e0e0] hover:bg-[#f8f8f8]">
                      <td
                        className="px-4 py-3 text-left font-500 text-[13px] text-[#242424] border-r border-[#e0e0e0] cursor-pointer"
                        onClick={() => handleSelectAll(resource.name)}
                      >
                        <div className="flex items-center gap-3">
                          <Checkbox
                            checked={resourceHasAll(resource.name)}
                            className="w-4 h-4 rounded-[2px] border-[#0066cc]"
                          />
                          {resource.label}
                        </div>
                      </td>
                      {ACTIONS.map(action => (
                        <td
                          key={`${resource.name}-${action}`}
                          className="px-4 py-3 text-center border-r border-[#e0e0e0]"
                        >
                          <Checkbox
                            checked={hasPermission(resource.name, action)}
                            onCheckedChange={() => togglePermission(resource.name, action)}
                            className="w-4 h-4 rounded-[2px] border-[#0066cc]"
                          />
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="text-[12px] text-[#898989]">
              Tổng: <span className="font-semibold text-[#242424]">{formData.permissions.length}</span> quyền được chọn
            </div>
          </div>
        </div>

        <DialogFooter className="border-t border-[#e0e0e0] pt-4 flex gap-2 justify-end sticky bottom-0 bg-white">
          <Button
            variant="outline"
            onClick={onClose}
            className="h-10 px-4 border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] rounded-[4px]"
          >
            Hủy
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading || !formData.name || formData.permissions.length === 0}
            className="h-10 px-4 bg-[#0066cc] hover:bg-[#004499] text-white font-semibold rounded-[4px]"
          >
            {loading ? 'Đang lưu...' : 'Lưu'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
