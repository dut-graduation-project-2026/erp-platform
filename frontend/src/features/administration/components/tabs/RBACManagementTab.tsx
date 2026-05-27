// src/features/administration/components/tabs/RBACManagementTab.tsx
// RBAC Management Tab - Dynamic role and permission management
// Includes Permission Matrix and Role Definition

'use client';

import React, { useState } from 'react';
import { Plus, Search, Edit2, Trash2, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useRoles } from '../../hooks/useRoles';
import { RolePermissionMatrix } from '../RolePermissionMatrix';
import { RoleFormDialog } from '../dialogs/RoleFormDialog';
import type { Role } from '../../types';

interface RBACManagementTabProps {
  organizationId: string;
}


export const RBACManagementTab: React.FC<RBACManagementTabProps> = ({ organizationId }) => {
  const { roles, createRole, updateRole, deleteRole } = useRoles();
  const [searchTerm, setSearchTerm] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);

  const orgRoles = roles.filter(r => r.organizationId === organizationId);
  const filteredRoles = orgRoles.filter(role =>
    role.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleEdit = (role: Role) => {
    setEditingRole(role);
    setShowForm(true);
  };

  const handleDelete = async (role: Role) => {
    if (confirm(`Bạn có chắc chắn muốn xóa vai trò ${role.name}?`)) {
      await deleteRole(role.id);
    }
  };

  return (
    <div className="space-y-8">
      {/* Roles Management Section */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-[24px] font-bold text-[#242424]">Quản lý Vai trò</h2>
            <p className="text-[14px] text-[#898989] mt-1">
              {filteredRoles.length} vai trò trong tổ chức này
            </p>
          </div>
          <Button
            onClick={() => {
              setEditingRole(null);
              setShowForm(true);
            }}
            className="bg-[#0066cc] hover:bg-[#004499] text-white font-semibold h-10 px-4 rounded-[4px]"
          >
            <Plus className="w-4 h-4 mr-2" />
            Tạo Vai trò
          </Button>
        </div>

        {/* Search */}
        <div className="flex items-center gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input
              placeholder="Tìm kiếm vai trò..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 h-10 rounded-[4px] border-[#d0d0d0] focus:border-[#0066cc]"
            />
          </div>
        </div>

        {/* Roles Table */}
        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <div className="overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Tên Vai trò</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Số Quyền</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Ngày tạo</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424] text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredRoles.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="text-center py-12">
                      <div className="flex flex-col items-center justify-center">
                        <AlertCircle className="w-8 h-8 mb-2 text-[#898989] opacity-50" />
                        <p className="text-[14px] text-[#898989]">Không có vai trò nào</p>
                        <Button
                          variant="link"
                          onClick={() => setShowForm(true)}
                          className="mt-2 text-[#0066cc]"
                        >
                          Tạo vai trò đầu tiên
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredRoles.map((role) => (
                    <TableRow
                      key={role.id}
                      className="border-b border-[#e0e0e0] hover:bg-[#f0f4ff] transition-colors cursor-pointer"
                      onClick={() => setSelectedRole(role)}
                    >
                      <TableCell className="text-[13px] text-[#242424] font-500">
                        {role.name}
                      </TableCell>
                      <TableCell>
                        <Badge className="bg-blue-100 text-blue-800 font-500">
                          {role.permissions.length}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-[13px] text-[#898989]">
                        {new Date(role.createdAt).toLocaleDateString('vi-VN')}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEdit(role);
                            }}
                            className="h-8 px-2 text-[#0066cc] hover:bg-[#f0f4ff]"
                          >
                            <Edit2 className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDelete(role);
                            }}
                            className="h-8 px-2 text-[#dc3545] hover:bg-[#ffe6e6]"
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </Card>
      </div>

      {/* Permission Matrix Preview */}
      {selectedRole && (
        <div className="space-y-6 pt-8 border-t border-[#e0e0e0]">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-[24px] font-bold text-[#242424]">Ma trận Quyền</h3>
              <p className="text-[14px] text-[#898989] mt-1">
                Vai trò: <span className="font-semibold text-[#242424]">{selectedRole.name}</span>
              </p>
            </div>
            <Button
              variant="outline"
              onClick={() => setSelectedRole(null)}
              className="h-10 px-4 border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] rounded-[4px]"
            >
              Đóng
            </Button>
          </div>

          <RolePermissionMatrix role={selectedRole} />
        </div>
      )}

      {/* Role Form Dialog */}
      {showForm && (
        <RoleFormDialog
          role={editingRole}
          organizationId={organizationId}
          onClose={() => {
            setShowForm(false);
            setEditingRole(null);
          }}
          onSubmit={async (data) => {
            if (editingRole) {
              await updateRole(editingRole.id, data);
            } else {
              await createRole({ ...data, organizationId });
            }
            setShowForm(false);
            setEditingRole(null);
          }}
        />
      )}
    </div>
  );
};
