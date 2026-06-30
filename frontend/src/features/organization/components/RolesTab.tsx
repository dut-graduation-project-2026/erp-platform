"use client";

import React, { useState } from "react";
import { Search, Plus, MoreHorizontal, ShieldCheck, LayoutGrid, List as ListIcon, ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import Link from "next/link";
import { APP_ROUTES } from "@/config/constants";
import { useRoles } from "@/features/organization/hooks/useRoles";
import { TablePagination } from "@/components/ui/table-pagination";

export function RolesTab({ orgId }: { orgId: string }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [viewMode, setViewMode] = useState<"kanban" | "list">("kanban");
  
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(10);
  
  const { roles, loading, error, totalElements, totalPages } = useRoles(orgId, page, limit);

  const filteredRoles = roles.filter(r => r.name.toLowerCase().includes(searchTerm.toLowerCase()));

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    setPage(1);
  };


  return (
    <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif] border border-[#e0e0e0] rounded-[8px] overflow-hidden shadow-[0px_1px_3px_rgba(0,0,0,0.05)]">
      {/* Top Control Bar */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 py-4 flex flex-col gap-4 z-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <PermissionGuard permission={PERMISSIONS.ROLES.CREATE}>
              <Link href={APP_ROUTES.ADMINISTRATION.ROLE_NEW(orgId)}>
                <button className="bg-[#0066cc] text-white px-4 py-2 rounded-[4px] text-[14px] font-[600] hover:bg-[#004499] transition-colors flex items-center gap-2">
                  <Plus className="w-4 h-4" />
                  New Role
                </button>
              </Link>
            </PermissionGuard>
          </div>
          
          <div className="flex items-center gap-4">
            <div className="relative">
              <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search roles..."
                className="pl-9 pr-4 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] w-[250px]"
                value={searchTerm}
                onChange={handleSearchChange}
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
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
        {loading ? (
          <div className="flex justify-center items-center h-full">
            <Loader2 className="w-8 h-8 text-[#0066cc] animate-spin" />
          </div>
        ) : error ? (
          <div className="text-center text-[#dc3545] p-8">{error}</div>
        ) : viewMode === "kanban" ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-[1200px] mx-auto">
            {filteredRoles.map((role) => (
              <div 
                key={role.id} 
                className="bg-white rounded-[8px] p-5 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-shadow border border-[#e0e0e0] hover:border-[#0066cc] flex flex-col h-full"
              >
                <div className="flex justify-between items-start mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-[#f0f4ff] flex items-center justify-center text-[#0066cc]">
                      <ShieldCheck className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-[16px] font-[600] text-[#242424]">{role.name}</h3>
                      <p className="text-[12px] text-[#898989] mt-0.5">Role inside org</p>
                    </div>
                  </div>
                </div>
                
                <div className="flex-1"></div>

                <div className="mt-5 pt-4 border-t border-[#e0e0e0] flex justify-between items-center">
                  <div className="flex -space-x-2">
                    <div className="w-7 h-7 rounded-full bg-[#d0d0d0] border-2 border-white flex items-center justify-center text-[10px] text-white font-[600] overflow-hidden">
                      ?
                    </div>
                  </div>
                  <Link href={APP_ROUTES.ADMINISTRATION.ROLE_DETAIL(orgId, role.id)}>
                    <button className="text-[#0066cc] text-[13px] font-[600] hover:underline">
                      Edit Permissions
                    </button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="max-w-[1200px] mx-auto bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col h-full">
            <div className="flex-1 overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                    <th className="py-3 px-4 text-[13px] font-[600] text-[#242424]">ROLE NAME</th>
                    <th className="py-3 px-4 text-[13px] font-[600] text-[#242424]">DESCRIPTION</th>
                    <th className="py-3 px-4 w-[100px] text-center">ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRoles.map((role, idx) => (
                    <tr 
                      key={role.id} 
                      className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors ${idx % 2 === 1 ? 'bg-[#fafafa]' : 'bg-white'}`}
                    >
                      <td className="py-3 px-4 text-[13px] text-[#242424] font-[600]">{role.name}</td>
                      <td className="py-3 px-4 text-[13px] text-[#666]">-</td>
                      <td className="py-3 px-4 text-center">
                        <Link href={APP_ROUTES.ADMINISTRATION.ROLE_DETAIL(orgId, role.id)}>
                          <button className="text-[#0066cc] text-[13px] hover:underline font-[600]">
                            Edit
                          </button>
                        </Link>
                      </td>
                    </tr>
                  ))}
                  {filteredRoles.length === 0 && (
                    <tr>
                      <td colSpan={3} className="py-8 text-center text-[#898989] text-[13px]">
                        No roles found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {!loading && totalElements > 0 && (
          <TablePagination
            page={page}
            limit={limit}
            totalItems={totalElements}
            totalPages={totalPages}
            onPageChange={setPage}
            onLimitChange={(newLimit) => {
              setLimit(newLimit);
              setPage(1);
            }}
          />
        )}
      </div>
    </div>
  );
}
