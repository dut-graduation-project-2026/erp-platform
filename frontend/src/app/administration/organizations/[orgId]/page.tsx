// src/app/administration/organizations/[orgId]/page.tsx
// Organization Detail Page - Settings for specific organization
// Includes User Management, RBAC, and configuration

'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import { ArrowLeft, Users, Shield, Settings } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useOrganizations } from '@/features/administration/hooks/useOrganizations';
import { UserManagementTab } from '@/features/administration/components/tabs/UserManagementTab';
import { RBACManagementTab } from '@/features/administration/components/tabs/RBACManagementTab';
import { SettingsTab } from '@/features/administration/components/tabs/SettingsTab';

export default function OrganizationDetailPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const { organizations } = useOrganizations();
  const organization = organizations.find(o => o.id === orgId);

  if (!organization) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-[24px] font-bold text-[#242424]">Tổ chức không tìm thấy</h2>
          <p className="text-[14px] text-[#898989] mt-2">Vui lòng quay lại danh sách tổ chức</p>
          <Button
            onClick={() => window.history.back()}
            className="mt-6 bg-[#0066cc] hover:bg-[#004499] text-white"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            Quay lại
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="border-b border-[#e0e0e0] bg-white sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-6 py-6">
          <div className="flex items-center gap-4 mb-6">
            <Button
              variant="ghost"
              onClick={() => window.history.back()}
              className="h-10 px-2 text-[#0066cc] hover:bg-[#f0f4ff]"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div>
              <h1 className="text-[32px] font-bold text-[#242424]">{organization.name}</h1>
              <p className="text-[12px] text-[#898989] uppercase tracking-[0.1px] mt-1">
                Mã: {organization.code}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs Navigation */}
      <div className="max-w-7xl mx-auto px-6 py-8">
        <Tabs defaultValue="users" className="w-full">
          <TabsList className="grid w-full grid-cols-3 bg-[#f8f8f8] rounded-[4px] p-1">
            <TabsTrigger
              value="users"
              className="flex items-center gap-2 rounded-[4px] text-[14px] font-500 data-[state=active]:bg-white data-[state=active]:text-[#0066cc] data-[state=active]:shadow-[0px_1px_3px_rgba(0,0,0,0.12)]"
            >
              <Users className="w-4 h-4" />
              Quản lý Người dùng
            </TabsTrigger>
            <TabsTrigger
              value="rbac"
              className="flex items-center gap-2 rounded-[4px] text-[14px] font-500 data-[state=active]:bg-white data-[state=active]:text-[#0066cc] data-[state=active]:shadow-[0px_1px_3px_rgba(0,0,0,0.12)]"
            >
              <Shield className="w-4 h-4" />
              Phân quyền (RBAC)
            </TabsTrigger>
            <TabsTrigger
              value="settings"
              className="flex items-center gap-2 rounded-[4px] text-[14px] font-500 data-[state=active]:bg-white data-[state=active]:text-[#0066cc] data-[state=active]:shadow-[0px_1px_3px_rgba(0,0,0,0.12)]"
            >
              <Settings className="w-4 h-4" />
              Cài đặt
            </TabsTrigger>
          </TabsList>

          {/* Users Tab */}
          <TabsContent value="users" className="mt-8">
            <UserManagementTab organizationId={orgId} />
          </TabsContent>

          {/* RBAC Tab */}
          <TabsContent value="rbac" className="mt-8">
            <RBACManagementTab organizationId={orgId} />
          </TabsContent>

          {/* Settings Tab */}
          <TabsContent value="settings" className="mt-8">
            <SettingsTab organizationId={orgId} organization={organization} />
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
