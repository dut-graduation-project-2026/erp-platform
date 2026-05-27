// src/features/administration/components/OrganizationList.tsx
// Organization List Component - Displays organizations in a table
// Follows DESIGN.md: DataTable pattern, Segoe UI, proper spacing

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Edit, Trash2 } from 'lucide-react';
import type { Organization } from '../types';

interface OrganizationListProps {
  organizations: Organization[];
  onAdd: () => void;
  onEdit: (org: Organization) => void;
  onDelete: (org: Organization) => void;
}

export const OrganizationList: React.FC<OrganizationListProps> = ({
  organizations,
  onAdd,
  onEdit,
  onDelete,
}) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  return (
    <Card className="shadow-standard">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-2xl font-semibold text-charcoal">
          Quản lý Tổ chức
        </CardTitle>
        <Button
          onClick={onAdd}
          className="bg-primary-blue hover:bg-dark-blue text-white"
        >
          <Plus className="w-4 h-4 mr-2" />
          Thêm Tổ chức
        </Button>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow className="bg-light-gray border-b border-border-gray">
              <TableHead className="font-semibold text-charcoal">Tên Tổ chức</TableHead>
              <TableHead className="font-semibold text-charcoal">Mã Code</TableHead>
              <TableHead className="font-semibold text-charcoal">Email Liên hệ</TableHead>
              <TableHead className="font-semibold text-charcoal">Ngày Tạo</TableHead>
              <TableHead className="font-semibold text-charcoal text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {organizations.map((org) => (
              <TableRow key={org.id} className="hover:bg-light-blue">
                <TableCell className="font-medium text-charcoal">
                  <div className="flex items-center gap-3">
                    {org.logo && (
                      /* eslint-disable-next-line @next/next/no-img-element */
                      <img
                        src={org.logo}
                        alt={`${org.name} logo`}
                        className="w-8 h-8 rounded-full object-cover"
                      />
                    )}
                    {org.name}
                  </div>
                </TableCell>
                <TableCell>
                  <Badge variant="secondary" className="bg-light-gray text-charcoal">
                    {org.code}
                  </Badge>
                </TableCell>
                <TableCell className="text-mid-gray">{org.contactEmail || '-'}</TableCell>
                <TableCell className="text-mid-gray">{formatDate(org.createdAt)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onEdit(org)}
                      className="text-primary-blue hover:bg-light-blue"
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDelete(org)}
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
        {organizations.length === 0 && (
          <div className="text-center py-8 text-mid-gray">
            Chưa có tổ chức nào. Nhấn &quot;Thêm Tổ chức&quot; để tạo mới.
          </div>
        )}
      </CardContent>
    </Card>
  );
};
