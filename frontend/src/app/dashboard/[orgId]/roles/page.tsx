"use client";

import React, { useState } from "react";
import { Search, Plus, MoreHorizontal, ShieldCheck, LayoutGrid, List as ListIcon, ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useRoles } from "@/features/organization/hooks/useRoles";

export default function RolesPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [viewMode, setViewMode] = useState<"kanban" | "list">("kanban");
  const params = useParams();
  const orgId = params.orgId as string;
  
  const { roles, loading, error } = useRoles(orgId);

  return (
    <PermissionGuard 
      permission={PERMISSIONS.ROLES.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don&apos;t have permission to view roles.</div>}
    >
      <div className="h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif]">
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
              
              {/* View Toggle */}
              <div className="flex bg-[#f8f8f8] p-1 rounded-[4px] border border-[#e0e0e0]">
                <button 
                  onClick={() => setViewMode("kanban")}
                  className={`p-1.5 rounded-[2px] transition-colors ${viewMode === "kanban" ? "bg-white shadow-[0px_1px_2px_rgba(0,0,0,0.1)] text-[#0066cc]" : "text-[#898989] hover:text-[#242424]"}`}
                >
                  <LayoutGrid className="w-4 h-4" />
                </button>
                <button 
                  onClick={() => setViewMode("list")}
                  className={`p-1.5 rounded-[2px] transition-colors ${viewMode === "list" ? "bg-white shadow-[0px_1px_2px_rgba(0,0,0,0.1)] text-[#0066cc]" : "text-[#898989] hover:text-[#242424]"}`}
                >
                  <ListIcon className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <PermissionGuard permission={PERMISSIONS.ROLES.CREATE}>
              <Link href={`/dashboard/${orgId}/roles/new`}>
                <button className="bg-[#0066cc] text-white px-4 py-[10px] rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors flex items-center gap-2">
                  <Plus className="w-4 h-4" />
                  New Role
                </button>
              </Link>
            </PermissionGuard>
          </div>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 overflow-auto p-6">
          {loading ? (
            <div className="flex justify-center items-center h-full">
              <Loader2 className="w-8 h-8 text-[#0066cc] animate-spin" />
            </div>
          ) : error ? (
            <div className="text-center text-[#dc3545] p-8">{error}</div>
          ) : viewMode === "kanban" ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-[1200px] mx-auto">
              {roles.map((role) => (
                <div 
                  key={role.id} 
                  className="bg-white rounded-[8px] p-5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-shadow cursor-pointer border border-transparent hover:border-[#0066cc] flex flex-col h-full"
                >
                  <div className="flex justify-between items-start mb-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-[#f0f4ff] flex items-center justify-center text-[#0066cc]">
                        <ShieldCheck className="w-5 h-5" />
                      </div>
                      <div>
                        <h3 className="text-[16px] font-bold text-[#242424]">{role.name}</h3>
                        <p className="text-[12px] text-[#898989] mt-0.5">Role inside org</p>
                      </div>
                    </div>
                    <button className="text-[#898989] hover:text-[#242424] p-1">
                      <MoreHorizontal className="w-4 h-4" />
                    </button>
                  </div>
                  
                  <div className="flex-1"></div>

                  <div className="mt-5 pt-4 border-t border-[#e0e0e0] flex justify-between items-center">
                    <div className="flex -space-x-2">
                      <div className="w-7 h-7 rounded-full bg-[#d0d0d0] border-2 border-white flex items-center justify-center text-[10px] text-white font-bold overflow-hidden">
                        ?
                      </div>
                    </div>
                    <Link href={`/dashboard/${orgId}/roles/${role.id}`}>
                      <button className="text-[#0066cc] text-[13px] font-semibold hover:underline">
                        Edit Permissions
                      </button>
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="max-w-[1200px] mx-auto bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                    <th className="py-3 px-4 w-[40px]">
                      <input type="checkbox" className="w-4 h-4 accent-[#0066cc]" />
                    </th>
                    <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Role Name</th>
                    <th className="py-3 px-4 text-[13px] font-semibold text-[#242424]">Description</th>
                    <th className="py-3 px-4 w-[100px] text-center">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {roles.map((role, idx) => (
                    <tr 
                      key={role.id} 
                      className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer ${idx % 2 === 1 ? 'bg-[#fafafa]' : 'bg-white'}`}
                    >
                      <td className="py-3 px-4" onClick={(e) => e.stopPropagation()}>
                        <input type="checkbox" className="w-4 h-4 accent-[#0066cc]" />
                      </td>
                      <td className="py-3 px-4 text-[13px] text-[#242424] font-medium">{role.name}</td>
                      <td className="py-3 px-4 text-[13px] text-[#666]">-</td>
                      <td className="py-3 px-4 text-center">
                        <Link href={`/dashboard/${orgId}/roles/${role.id}`}>
                          <button className="text-[#0066cc] text-[13px] hover:underline font-medium">
                            Edit
                          </button>
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="bg-white border-t border-[#e0e0e0] px-4 py-3 flex items-center justify-between text-[13px] text-[#898989]">
                <span>Showing 1 to {roles.length} of {roles.length} entries</span>
                <div className="flex items-center gap-1">
                  <button className="p-1 hover:bg-[#f8f8f8] rounded disabled:opacity-50"><ChevronLeft className="w-4 h-4" /></button>
                  <button className="px-2 py-1 bg-[#0066cc] text-white rounded">1</button>
                  <button className="p-1 hover:bg-[#f8f8f8] rounded disabled:opacity-50"><ChevronRight className="w-4 h-4" /></button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </PermissionGuard>
  );
}
