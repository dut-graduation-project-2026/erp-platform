"use client";

import React, { useState } from "react";
import { Search, Plus, Filter, MoreHorizontal, ShieldCheck } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";

const mockRoles = [
  { id: "1", name: "SYSTEM_ADMIN", usersCount: 2, description: "Full system access including module configuration" },
  { id: "2", name: "ORG_ADMIN", usersCount: 5, description: "Full access within the organization scope" },
  { id: "3", name: "STAFF", usersCount: 45, description: "Standard user access" },
  { id: "4", name: "HR_MANAGER", usersCount: 3, description: "Manage human resources and payroll" },
];

export default function RolesPage() {
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <PermissionGuard 
      permission={PERMISSIONS.ROLES.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don't have permission to view roles.</div>}
    >
      <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif]">
        {/* Top Control Bar */}
        <div className="border-b border-[#e0e0e0] px-6 py-4 flex flex-col gap-4 bg-white sticky top-0 z-10">
          <div className="flex items-center text-[13px]">
            <span className="text-[#898989] cursor-pointer hover:underline">Dashboard</span>
            <span className="mx-2 text-[#898989]">{'>'}</span>
            <span className="text-[#0066cc] font-medium">Roles & Permissions</span>
          </div>

          <div className="flex items-center justify-between">
            <h1 className="text-[32px] font-bold text-[#242424] leading-[1.2]">Roles</h1>
            
            <div className="flex items-center gap-4">
              <div className="relative">
                <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search roles..."
                  className="pl-9 pr-4 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] w-[250px]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <PermissionGuard permission={PERMISSIONS.ROLES.CREATE}>
              <button className="bg-[#0066cc] text-white px-4 py-[10px] rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors flex items-center gap-2">
                <Plus className="w-4 h-4" />
                New Role
              </button>
            </PermissionGuard>
          </div>
        </div>

        {/* Main Content Area - Kanban/Card View for Roles */}
        <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-[1200px] mx-auto">
            {mockRoles.map((role) => (
              <div 
                key={role.id} 
                className="bg-white rounded-[8px] p-5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-shadow cursor-pointer border border-transparent hover:border-[#0066cc]"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-[#f0f4ff] flex items-center justify-center text-[#0066cc]">
                      <ShieldCheck className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-[16px] font-bold text-[#242424]">{role.name}</h3>
                      <p className="text-[12px] text-[#898989] mt-0.5">{role.usersCount} Users assigned</p>
                    </div>
                  </div>
                  <button className="text-[#898989] hover:text-[#242424] p-1">
                    <MoreHorizontal className="w-4 h-4" />
                  </button>
                </div>
                
                <p className="text-[14px] text-[#666] line-clamp-2 min-h-[42px]">
                  {role.description}
                </p>

                <div className="mt-5 pt-4 border-t border-[#e0e0e0] flex justify-between items-center">
                  <div className="flex -space-x-2">
                    {[...Array(Math.min(role.usersCount, 3))].map((_, i) => (
                      <div key={i} className="w-7 h-7 rounded-full bg-[#d0d0d0] border-2 border-white flex items-center justify-center text-[10px] text-white font-bold overflow-hidden">
                        U{i+1}
                      </div>
                    ))}
                    {role.usersCount > 3 && (
                      <div className="w-7 h-7 rounded-full bg-[#f8f8f8] border-2 border-white flex items-center justify-center text-[10px] text-[#898989] font-bold">
                        +{role.usersCount - 3}
                      </div>
                    )}
                  </div>
                  <button className="text-[#0066cc] text-[13px] font-semibold hover:underline">
                    Edit Permissions
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
}
