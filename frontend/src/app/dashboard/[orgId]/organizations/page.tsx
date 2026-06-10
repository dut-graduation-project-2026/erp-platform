"use client";

import React, { useState } from "react";
import { Search, Plus, Filter, MoreHorizontal, Building2, MapPin, Phone } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";

const mockOrgs = [
  { id: "1", name: "Công ty Cổ phần Công nghệ DUT", address: "Đà Nẵng", phone: "02363841111", taxCode: "0400123456", users: 156 },
  { id: "2", name: "Chi nhánh Hà Nội", address: "Hà Nội", phone: "02431234567", taxCode: "0400123456-001", users: 42 },
  { id: "3", name: "Chi nhánh TP.HCM", address: "Hồ Chí Minh", phone: "02831234567", taxCode: "0400123456-002", users: 89 },
];

export default function OrganizationsPage() {
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <PermissionGuard 
      permission={PERMISSIONS.ORGANIZATIONS.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don&apos;t have permission to view organizations.</div>}
    >
      <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif]">
        {/* Top Control Bar */}
        <div className="border-b border-[#e0e0e0] px-6 py-4 flex flex-col gap-4 bg-white sticky top-0 z-10">
          <div className="flex items-center text-[13px]">
            <span className="text-[#898989] cursor-pointer hover:underline">Dashboard</span>
            <span className="mx-2 text-[#898989]">{'>'}</span>
            <span className="text-[#0066cc] font-medium">Organizations</span>
          </div>

          <div className="flex items-center justify-between">
            <h1 className="text-[32px] font-bold text-[#242424] leading-[1.2]">Organizations</h1>
            
            <div className="flex items-center gap-4">
              <div className="relative">
                <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search organizations..."
                  className="pl-9 pr-4 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] w-[250px]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <button className="flex items-center gap-2 px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[#242424] text-[14px] hover:bg-[#f8f8f8] transition-colors">
                <Filter className="w-4 h-4" />
                <span>Filters</span>
              </button>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <PermissionGuard permission={PERMISSIONS.ORGANIZATIONS.CREATE}>
              <button className="bg-[#0066cc] text-white px-4 py-[10px] rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors flex items-center gap-2">
                <Plus className="w-4 h-4" />
                New Organization
              </button>
            </PermissionGuard>
          </div>
        </div>

        {/* Main Content Area - Kanban View for Organizations */}
        <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-[1200px] mx-auto">
            {mockOrgs.map((org) => (
              <div 
                key={org.id} 
                className="bg-white rounded-[8px] p-5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-shadow cursor-pointer border border-transparent hover:border-[#0066cc]"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-[4px] bg-[#f8f8f8] border border-[#e0e0e0] flex items-center justify-center text-[#242424]">
                      <Building2 className="w-6 h-6" strokeWidth={1.5} />
                    </div>
                  </div>
                  <button className="text-[#898989] hover:text-[#242424] p-1">
                    <MoreHorizontal className="w-4 h-4" />
                  </button>
                </div>
                
                <h3 className="text-[16px] font-bold text-[#242424] mb-3 line-clamp-1" title={org.name}>
                  {org.name}
                </h3>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center gap-2 text-[13px] text-[#666]">
                    <MapPin className="w-4 h-4 text-[#898989]" />
                    <span className="line-clamp-1">{org.address}</span>
                  </div>
                  <div className="flex items-center gap-2 text-[13px] text-[#666]">
                    <Phone className="w-4 h-4 text-[#898989]" />
                    <span>{org.phone}</span>
                  </div>
                </div>

                <div className="mt-4 pt-4 border-t border-[#e0e0e0] flex justify-between items-center text-[13px]">
                  <span className="text-[#898989]">Tax: {org.taxCode}</span>
                  <span className="font-semibold text-[#0066cc] bg-[#f0f4ff] px-2 py-1 rounded-[4px]">
                    {org.users} Users
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
}
