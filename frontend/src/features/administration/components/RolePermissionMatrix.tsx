// src/features/administration/components/RolePermissionMatrix.tsx
// Role Permission Matrix - Display and edit permissions
// Follows DESIGN.md: Grid layout, checkboxes, proper spacing

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import type { Role } from '../types';

interface RolePermissionMatrixProps {
  role: Role;
}

const RESOURCES = [
  { name: 'Sales', color: 'bg-blue-50', label: 'Bán hàng' },
  { name: 'Inventory', color: 'bg-green-50', label: 'Kho & Chuỗi cung ứng' },
  { name: 'Finance', color: 'bg-purple-50', label: 'Tài chính' },
  { name: 'HR', color: 'bg-orange-50', label: 'Nhân sự' },
  { name: 'Blockchain Audit', color: 'bg-indigo-50', label: 'Kiểm toán Blockchain' },
];

const ACTIONS = [
  { key: 'VIEW', label: 'Xem', icon: '👁️' },
  { key: 'CREATE', label: 'Tạo', icon: '➕' },
  { key: 'EDIT', label: 'Sửa', icon: '✏️' },
  { key: 'DELETE', label: 'Xóa', icon: '🗑️' },
  { key: 'EXPORT', label: 'Xuất', icon: '📥' },
];

export const RolePermissionMatrix: React.FC<RolePermissionMatrixProps> = ({ role }) => {
  const hasPermission = (resource: string, action: string) => {
    const permissionString = `${resource.toLowerCase()}:${action.toLowerCase()}`;
    return role.permissions.includes(permissionString);
  };

  const permissionCount = role.permissions.length;

  return (
    <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
      <CardHeader className="border-b border-[#e0e0e0] pb-4">
        <div className="flex items-center justify-between">
          <CardTitle className="text-[18px] font-bold text-[#242424]">
            Ma trận Phân quyền
          </CardTitle>
          <Badge className="bg-blue-100 text-blue-800 font-600">
            {permissionCount} quyền
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="pt-6">
        <div className="overflow-x-auto">
          {/* Permission Matrix Table */}
          <div className="min-w-[900px]">
            {/* Header Row */}
            <div className="grid gap-0 border border-[#e0e0e0] rounded-[4px] overflow-hidden">
              {/* Top-left corner (Resource label) */}
              <div className="col-span-full grid grid-cols-6 gap-0">
                <div className="bg-[#f8f8f8] p-4 border-r border-[#e0e0e0] border-b border-[#e0e0e0] font-semibold text-[13px] text-[#242424]">
                  Tài nguyên / Hành động
                </div>

                {/* Action Headers */}
                {ACTIONS.map((action) => (
                  <div
                    key={action.key}
                    className="bg-[#f8f8f8] p-4 border-r border-[#e0e0e0] border-b border-[#e0e0e0] font-semibold text-[13px] text-[#242424] text-center"
                  >
                    <div className="text-lg mb-1">{action.icon}</div>
                    <div>{action.label}</div>
                  </div>
                ))}
              </div>

              {/* Resource Rows */}
              {RESOURCES.map((resource) => (
                <div key={resource.name} className="col-span-full grid grid-cols-6 gap-0">
                  {/* Resource Label */}
                  <div
                    className={`${resource.color} p-4 border-r border-[#e0e0e0] border-b border-[#e0e0e0] font-500 text-[13px] text-[#242424]`}
                  >
                    {resource.label}
                  </div>

                  {/* Permission Checkboxes */}
                  {ACTIONS.map((action) => (
                    <div
                      key={`${resource.name}-${action.key}`}
                      className="p-4 border-r border-[#e0e0e0] border-b border-[#e0e0e0] flex items-center justify-center bg-white hover:bg-[#f8f8f8] transition-colors"
                    >
                      <Checkbox
                        checked={hasPermission(resource.name, action.key)}
                        disabled
                        className="w-5 h-5 rounded-[2px] border-[#0066cc]"
                      />
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Permission List */}
        <div className="mt-8 pt-8 border-t border-[#e0e0e0]">
          <h4 className="text-[14px] font-semibold text-[#242424] mb-4">Danh sách Quyền</h4>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
            {role.permissions.length === 0 ? (
              <div className="col-span-full text-[13px] text-[#898989] py-4">
                Không có quyền nào được gán
              </div>
            ) : (
              role.permissions.map((permission) => (
                <Badge
                  key={permission}
                  className="bg-blue-100 text-blue-800 font-500 text-[12px] justify-start"
                >
                  {permission.toUpperCase()}
                </Badge>
              ))
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
