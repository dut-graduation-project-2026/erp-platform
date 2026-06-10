// src/features/administration/components/UserList.tsx
// User List Component - Displays users in a table
// Follows DESIGN.md: DataTable pattern, Segoe UI, proper spacing

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Edit, Trash2, UserCheck, UserX } from 'lucide-react';
import type { User, Role } from '../types';

interface UserListProps {
  users: User[];
  roles: Role[];
  onAdd: () => void;
  onEdit: (user: User) => void;
  onDelete: (user: User) => void;
  onToggleStatus: (user: User) => void;
}

export const UserList: React.FC<UserListProps> = ({
  users,
  roles,
  onAdd,
  onEdit,
  onDelete,
  onToggleStatus,
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const getRoleName = (roleId?: string) => {
    if (!roleId) return 'Chưa gán';
    const role = roles.find(r => r.id === roleId);
    return role?.name || 'Không tìm thấy';
  };

  return (
    <Card className="shadow-standard">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-2xl font-semibold text-charcoal">
          Quản lý Người dùng
        </CardTitle>
        <Button
          onClick={onAdd}
          className="bg-primary-blue hover:bg-dark-blue text-white"
        >
          <Plus className="w-4 h-4 mr-2" />
          Thêm Người dùng
        </Button>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow className="bg-light-gray border-b border-border-gray">
              <TableHead className="font-semibold text-charcoal">Họ và Tên</TableHead>
              <TableHead className="font-semibold text-charcoal">Email</TableHead>
              <TableHead className="font-semibold text-charcoal">Vai trò</TableHead>
              <TableHead className="font-semibold text-charcoal">Trạng thái</TableHead>
              <TableHead className="font-semibold text-charcoal">Ngày Tạo</TableHead>
              <TableHead className="font-semibold text-charcoal text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.id} className="hover:bg-light-blue">
                <TableCell className="font-medium text-charcoal">
                  {user.firstName} {user.lastName}
                </TableCell>
                <TableCell className="text-mid-gray">{user.email}</TableCell>
                <TableCell>
                  <Badge
                    variant={user.roleId ? "default" : "secondary"}
                    className={user.roleId ? "bg-primary-blue text-white" : "bg-light-gray text-charcoal"}
                  >
                    {getRoleName(user.roleId)}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge
                    variant={user.isActive ? "default" : "destructive"}
                    className={user.isActive ? "bg-success-green text-white" : "bg-error-red text-white"}
                  >
                    {user.isActive ? 'Hoạt động' : 'Không hoạt động'}
                  </Badge>
                </TableCell>
                <TableCell className="text-mid-gray">{formatDate(user.createdAt)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onToggleStatus(user)}
                      className={user.isActive ? "text-error-red hover:bg-light-red" : "text-success-green hover:bg-light-green"}
                      title={user.isActive ? "Vô hiệu hóa" : "Kích hoạt"}
                    >
                      {user.isActive ? <UserX className="w-4 h-4" /> : <UserCheck className="w-4 h-4" />}
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onEdit(user)}
                      className="text-primary-blue hover:bg-light-blue"
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDelete(user)}
                      className="text-error-red hover:bg-light-red"
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {users.length === 0 && (
          <div className="text-center py-8 text-mid-gray">
            Chưa có người dùng nào. Nhấn &quot;Thêm Người dùng&quot; để tạo mới.
          </div>
        )}
      </CardContent>
    </Card>
  );
};
