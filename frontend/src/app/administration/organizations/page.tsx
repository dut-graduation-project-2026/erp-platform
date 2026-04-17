// src/app/administration/organizations/page.tsx
// System Admin Dashboard - Organization Management
// Enterprise-grade design following DESIGN.md system

'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Plus, Search, ChevronRight, Building2, Users, Settings, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useOrganizations } from '@/features/administration/hooks/useOrganizations';
import { OrganizationFormDialog } from '@/features/administration/components/dialogs/OrganizationFormDialog';
import { Organization } from '@/features/administration/types';

export default function OrganizationsPage() {
  const router = useRouter();
  const { organizations, createOrganization, updateOrganization, deleteOrganization } = useOrganizations();
  const [searchTerm, setSearchTerm] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null);

  const filteredOrgs = organizations.filter(org =>
    org.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    org.code.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleEdit = (org: Organization) => {
    setEditingOrg(org);
    setShowForm(true);
  };

  const handleNavigateToOrg = (orgId: string) => {
    router.push(`/administration/organizations/${orgId}`);
  };

  const stats = [
    { label: 'Tổng số Tổ chức', value: organizations.length, icon: Building2, color: 'bg-blue-50' },
    { label: 'Người dùng Tổng', value: organizations.length * 3, icon: Users, color: 'bg-green-50' },
  ];

  return (
    <div className="min-h-screen bg-white">
      {/* Page Header */}
      <div className="border-b border-[#e0e0e0] bg-white">
        <div className="max-w-7xl mx-auto px-6 py-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-[32px] font-bold leading-[1.2] text-[#242424]">
                Quản lý Tổ chức
              </h1>
              <p className="text-[14px] text-[#898989] mt-2">
                Quản lý danh sách các tổ chức sử dụng dịch vụ ERP Platform
              </p>
            </div>
            <Button
              onClick={() => {
                setEditingOrg(null);
                setShowForm(true);
              }}
              className="bg-[#0066cc] hover:bg-[#004499] text-white font-semibold h-10 px-4 rounded-[4px]"
            >
              <Plus className="w-4 h-4 mr-2" />
              Thêm Tổ chức
            </Button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-6 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          {stats.map((stat, idx) => (
            <div key={idx} className={`${stat.color} p-6 rounded-[4px] border border-[#e0e0e0]`}>
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-[12px] font-semibold text-[#898989] uppercase tracking-[0.1px]">
                    {stat.label}
                  </p>
                  <p className="text-[32px] font-bold text-[#242424] mt-2">
                    {stat.value}
                  </p>
                </div>
                <stat.icon className="w-12 h-12 text-[#0066cc] opacity-20" />
              </div>
            </div>
          ))}
        </div>

        {/* Search & Filter */}
        <div className="mb-6 flex items-center gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input
              placeholder="Tìm kiếm tổ chức..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10 h-10 rounded-[4px] border-[#d0d0d0] focus:border-[#0066cc]"
            />
          </div>
        </div>

        {/* Organizations Table */}
        <Card className="border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <div className="overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Tên Tổ chức</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Mã Code</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Email</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424]">Trạng thái</TableHead>
                  <TableHead className="font-semibold text-[13px] text-[#242424] text-right">Hành động</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredOrgs.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-12 text-[#898989]">
                      <div className="flex flex-col items-center justify-center">
                        <AlertCircle className="w-8 h-8 mb-2 opacity-50" />
                        <p>Không có tổ chức nào</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredOrgs.map((org) => (
                    <TableRow
                      key={org.id}
                      className="border-b border-[#e0e0e0] hover:bg-[#f0f4ff] transition-colors cursor-pointer"
                      onClick={() => handleNavigateToOrg(org.id)}
                    >
                      <TableCell className="text-[13px] text-[#242424] font-500">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-[#0066cc] rounded-[4px] flex items-center justify-center text-white text-[12px] font-bold">
                            {org.name.charAt(0)}
                          </div>
                          {org.name}
                        </div>
                      </TableCell>
                      <TableCell className="text-[13px] text-[#898989]">{org.code}</TableCell>
                      <TableCell className="text-[13px] text-[#898989]">{org.contactEmail || '-'}</TableCell>
                      <TableCell>
                        <Badge className="bg-green-100 text-green-800 font-semibold">Hoạt động</Badge>
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleEdit(org);
                            }}
                            className="h-8 px-2 text-[#0066cc] hover:bg-[#f0f4ff]"
                          >
                            <Settings className="w-4 h-4" />
                          </Button>
                          <ChevronRight className="w-4 h-4 text-[#898989]" />
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

      {/* Organization Form Dialog */}
      {showForm && (
        <OrganizationFormDialog
          organization={editingOrg}
          onClose={() => {
            setShowForm(false);
            setEditingOrg(null);
          }}
          onSubmit={async (data) => {
            if (editingOrg) {
              await updateOrganization(editingOrg.id, data);
            } else {
              await createOrganization(data);
            }
            setShowForm(false);
            setEditingOrg(null);
          }}
        />
      )}
    </div>
  );
}
