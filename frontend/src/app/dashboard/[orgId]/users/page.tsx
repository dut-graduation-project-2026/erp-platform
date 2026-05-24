"use client";

import React, { useState } from "react";
import { Search, Plus, Filter, Download, MoreHorizontal, ChevronLeft, ChevronRight } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";

// Mock data
const mockUsers = [
  { id: "1", name: "Nguyễn Văn Giám Đốc", email: "director@dut.edu.vn", role: "ORG_ADMIN", status: "Active", lastLogin: "2026-05-24" },
  { id: "2", name: "Trần Thị Nhân Viên", email: "staff@dut.edu.vn", role: "STAFF", status: "Active", lastLogin: "2026-05-23" },
  { id: "3", name: "Lê Hoàng IT", email: "it@dut.edu.vn", role: "SYSTEM_ADMIN", status: "Inactive", lastLogin: "2026-05-10" },
];

export default function UsersPage() {
  const [searchTerm, setSearchTerm] = useState("");

  return (
    <PermissionGuard 
      permission={PERMISSIONS.USERS.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don't have permission to view users.</div>}
    >
      <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif]">
        {/* Top Control Bar (Odoo Style) */}
        <div className="border-b border-[#e0e0e0] px-6 py-4 flex flex-col gap-4">
          {/* Breadcrumb & Title */}
          <div className="flex items-center text-[13px]">
            <span className="text-[#898989] cursor-pointer hover:underline">Dashboard</span>
            <span className="mx-2 text-[#898989]">{'>'}</span>
            <span className="text-[#0066cc] font-medium">Users</span>
          </div>

          <div className="flex items-center justify-between">
            <h1 className="text-[32px] font-bold text-[#242424] leading-[1.2]">Users</h1>
            
            {/* Search & Filters */}
            <div className="flex items-center gap-4">
              <div className="relative">
                <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search users..."
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

          {/* Action Buttons */}
          <div className="flex items-center gap-2">
            <PermissionGuard permission={PERMISSIONS.USERS.CREATE}>
              <button className="bg-[#0066cc] text-white px-4 py-[10px] rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors flex items-center gap-2">
                <Plus className="w-4 h-4" />
                New User
              </button>
            </PermissionGuard>
            <button className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-[10px] rounded-[4px] text-[14px] font-semibold hover:bg-[#f8f8f8] transition-colors flex items-center gap-2">
              <Download className="w-4 h-4" />
              Export
            </button>
          </div>
        </div>

        {/* Main Content Area - Table */}
        <div className="flex-1 overflow-auto bg-white p-6">
          <div className="border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                  <th className="py-3 px-4 w-[40px]">
                    <input type="checkbox" className="w-4 h-4 accent-[#0066cc]" />
                  </th>
                  <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Name</th>
                  <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Email</th>
                  <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Role</th>
                  <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Status</th>
                  <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Last Login</th>
                  <th className="py-3 px-4 w-[50px]"></th>
                </tr>
              </thead>
              <tbody>
                {mockUsers.map((user, idx) => (
                  <tr 
                    key={user.id} 
                    className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer ${idx % 2 === 1 ? 'bg-[#fafafa]' : 'bg-white'}`}
                  >
                    <td className="py-3 px-4" onClick={(e) => e.stopPropagation()}>
                      <input type="checkbox" className="w-4 h-4 accent-[#0066cc]" />
                    </td>
                    <td className="py-3 px-4 text-[13px] text-[#242424]">{user.name}</td>
                    <td className="py-3 px-4 text-[13px] text-[#242424]">{user.email}</td>
                    <td className="py-3 px-4 text-[13px] text-[#242424]">
                      <span className="bg-[#e2e8f0] text-[#334155] px-2 py-1 rounded-[4px] text-[11px] font-medium">
                        {user.role}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-[13px]">
                      {user.status === 'Active' ? (
                        <span className="text-[#28a745] font-medium flex items-center gap-1">
                          <span className="w-2 h-2 rounded-full bg-[#28a745]"></span> Active
                        </span>
                      ) : (
                        <span className="text-[#898989] font-medium flex items-center gap-1">
                          <span className="w-2 h-2 rounded-full bg-[#898989]"></span> Inactive
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-[13px] text-[#898989]">{user.lastLogin}</td>
                    <td className="py-3 px-4 text-center">
                      <button className="text-[#898989] hover:text-[#242424]">
                        <MoreHorizontal className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            
            {/* Pagination Footer */}
            <div className="bg-white border-t border-[#e0e0e0] px-4 py-3 flex items-center justify-between text-[13px] text-[#898989]">
              <span>Showing 1 to 3 of 3 entries</span>
              <div className="flex items-center gap-1">
                <button className="p-1 hover:bg-[#f8f8f8] rounded disabled:opacity-50"><ChevronLeft className="w-4 h-4" /></button>
                <button className="px-2 py-1 bg-[#0066cc] text-white rounded">1</button>
                <button className="p-1 hover:bg-[#f8f8f8] rounded disabled:opacity-50"><ChevronRight className="w-4 h-4" /></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </PermissionGuard>
  );
}
