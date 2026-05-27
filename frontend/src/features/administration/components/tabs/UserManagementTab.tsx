// src/features/administration/components/tabs/UserManagementTab.tsx
// User Management Tab - Manage users in an organization
// Follows DESIGN.md: DataTable, forms, proper spacing

'use client';

import React, { useState } from 'react';
import { Plus, Search, Edit2, Trash2, AlertCircle, Users, UserCheck, UserX, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useUsers } from '../../hooks/useUsers';
import { UserFormDialog } from '../dialogs/UserFormDialog';
import type { User } from '../../types';

interface UserManagementTabProps {
  organizationId: string;
}

export const UserManagementTab: React.FC<UserManagementTabProps> = ({ organizationId }) => {
  const { users, createUser, updateUser, deleteUser } = useUsers();
  const [searchTerm, setSearchTerm] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  const orgUsers = users.filter(u => u.organizationId === organizationId);
  const filteredUsers = orgUsers.filter(user =>
    user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleEdit = (user: User) => {
    setEditingUser(user);
    setShowForm(true);
  };

  const handleDelete = async (user: User) => {
    if (confirm(`Bạn có chắc chắn muốn xóa người dùng ${user.firstName} ${user.lastName}?`)) {
      await deleteUser(user.id);
    }
  };

  return (
    <div className="space-y-6">
      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-[12px] font-semibold text-[#898989] uppercase tracking-wide">Tổng người dùng</p>
                <p className="text-[32px] font-bold text-[#242424] mt-1">{orgUsers.length}</p>
              </div>
              <div className="h-12 w-12 bg-[#f0f4ff] rounded-[8px] flex items-center justify-center">
                <Users className="h-6 w-6 text-[#0066cc]" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-[12px] font-semibold text-[#898989] uppercase tracking-wide">Đang hoạt động</p>
                <p className="text-[32px] font-bold text-[#28a745] mt-1">
                  {orgUsers.filter(u => u.isActive).length}
                </p>
              </div>
              <div className="h-12 w-12 bg-[#f0fff0] rounded-[8px] flex items-center justify-center">
                <UserCheck className="h-6 w-6 text-[#28a745]" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-[12px] font-semibold text-[#898989] uppercase tracking-wide">Không hoạt động</p>
                <p className="text-[32px] font-bold text-[#dc3545] mt-1">
                  {orgUsers.filter(u => !u.isActive).length}
                </p>
              </div>
              <div className="h-12 w-12 bg-[#fff0f0] rounded-[8px] flex items-center justify-center">
                <UserX className="h-6 w-6 text-[#dc3545]" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-[12px] font-semibold text-[#898989] uppercase tracking-wide">Vai trò</p>
                <p className="text-[32px] font-bold text-[#0066cc] mt-1">
                  {new Set(orgUsers.map(u => u.roleId).filter(Boolean)).size}
                </p>
              </div>
              <div className="h-12 w-12 bg-[#f0f4ff] rounded-[8px] flex items-center justify-center">
                <Shield className="h-6 w-6 text-[#0066cc]" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-[24px] font-bold text-[#242424]">Quản lý Người dùng</h2>
          <p className="text-[14px] text-[#898989] mt-1">
            {filteredUsers.length} người dùng trong tổ chức này
          </p>
        </div>
        <Button
          onClick={() => {
            setEditingUser(null);
            setShowForm(true);
          }}
          className="bg-[#0066cc] hover:bg-[#004499] text-white font-semibold h-10 px-4 rounded-[4px]"
        >
          <Plus className="w-4 h-4 mr-2" />
          Thêm Người dùng
        </Button>
      </div>

      {/* Search */}
      <div className="flex items-center gap-3">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
          <Input
            placeholder="Tìm kiếm người dùng..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10 h-10 rounded-[4px] border-[#d0d0d0] focus:border-[#0066cc]"
          />
        </div>
      </div>

      {/* Users Table */}
      <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
        <div className="overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                <TableHead className="font-semibold text-[13px] text-[#242424]">Họ tên</TableHead>
                <TableHead className="font-semibold text-[13px] text-[#242424]">Email</TableHead>
                <TableHead className="font-semibold text-[13px] text-[#242424]">Vai trò</TableHead>
                <TableHead className="font-semibold text-[13px] text-[#242424]">Trạng thái</TableHead>
                <TableHead className="font-semibold text-[13px] text-[#242424]">Ngày tạo</TableHead>
                <TableHead className="font-semibold text-[13px] text-[#242424] text-right">Hành động</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-12">
                    <div className="flex flex-col items-center justify-center">
                      <AlertCircle className="w-8 h-8 mb-2 text-[#898989] opacity-50" />
                      <p className="text-[14px] text-[#898989]">Không có người dùng nào</p>
                      <Button
                        variant="link"
                        onClick={() => setShowForm(true)}
                        className="mt-2 text-[#0066cc]"
                      >
                        Thêm người dùng đầu tiên
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ) : (
                filteredUsers.map((user) => (
                  <TableRow
                    key={user.id}
                    className="border-b border-[#e0e0e0] hover:bg-[#f0f4ff] transition-colors"
                  >
                    <TableCell className="text-[13px] text-[#242424]">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-[#0066cc] rounded-[4px] flex items-center justify-center text-white text-[12px] font-bold">
                          {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                        </div>
                        <span className="font-500">{user.firstName} {user.lastName}</span>
                      </div>
                    </TableCell>
                    <TableCell className="text-[13px] text-[#898989]">{user.email}</TableCell>
                    <TableCell>
                      <Badge className="bg-blue-100 text-blue-800 font-500 text-[11px]">
                        {user.roleId ? 'Quản lý' : 'Người dùng'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge
                        className={user.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}
                      >
                        {user.isActive ? 'Hoạt động' : 'Không hoạt động'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-[13px] text-[#898989]">
                      {new Date(user.createdAt).toLocaleDateString('vi-VN')}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(user)}
                          className="h-8 px-2 text-[#0066cc] hover:bg-[#f0f4ff]"
                        >
                          <Edit2 className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(user)}
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

      {/* User Form Dialog */}
      {showForm && (
        <UserFormDialog
          user={editingUser}
          organizationId={organizationId}
          onClose={() => {
            setShowForm(false);
            setEditingUser(null);
          }}
          onSubmit={async (data) => {
            if (editingUser) {
              await updateUser(editingUser.id, data);
            } else {
              await createUser({ ...data, organizationId });
            }
            setShowForm(false);
            setEditingUser(null);
          }}
        />
      )}
    </div>
  );
};
