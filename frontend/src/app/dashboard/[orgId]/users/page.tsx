"use client";

import React, { useState, useMemo, use } from "react";
import { Search, Plus, ChevronLeft, ChevronRight, UserCircle, RefreshCcw, SlidersHorizontal, LayoutList, LayoutGrid, Building2, X } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import { useOrganizationMembers } from "@/features/organization/hooks/useOrganizationMembers";
import { InviteUserModal } from "@/features/organization/components/InviteUserModal";
import { UserActionsDropdown } from "@/features/organization/components/UserActionsDropdown";
import { ChangeRoleModal } from "@/features/organization/components/ChangeRoleModal";
import { RemoveUserModal } from "@/features/organization/components/RemoveUserModal";
import { useRouter } from "next/navigation";

export default function UsersPage({ params }: { params: Promise<{ orgId: string }> }) {
  const unwrappedParams = use(params);
  const orgId = unwrappedParams.orgId;
  const router = useRouter();
  
  const [searchTerm, setSearchTerm] = useState("");
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [selectedUserForRole, setSelectedUserForRole] = useState<any>(null);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [selectedUserForRemove, setSelectedUserForRemove] = useState<any>(null);
  const [page, setPage] = useState(1);
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const limit = 10;
  
  const { members, loading, totalElements, totalPages, refresh } = useOrganizationMembers({
    organizationId: orgId,
    query: searchTerm,
    page: page, // backend is 1-indexed for page
    limit,
  });

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    setPage(1); // reset to first page on search
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    refresh();
  };

  const formattedUsers = useMemo(() => {
    return members.map((u) => {
      return {
        id: u.id,
        name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || 'Unknown User',
        email: u.email,
        roles: u.roles?.map(r => r.name) || [],
        status: u.status || 'Active',
        lastLogin: u.lastLogin || '-',
        primaryOrg: 'Current Organization'
      };
    });
  }, [members]);

  const navigateToDetails = (userId: string) => {
    router.push(`/dashboard/${orgId}/users/${userId}`);
  };

  const handleExport = () => {
    const csvContent = [
      ['Name', 'Email', 'Roles', 'Status', 'Last Login', 'Primary Organization'],
      ...formattedUsers.map(u => [
        `"${u.name}"`,
        `"${u.email}"`,
        `"${u.roles.join(', ')}"`,
        `"${u.status}"`,
        `"${u.lastLogin}"`,
        `"${u.primaryOrg}"`
      ])
    ].map(e => e.join(',')).join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'users_export.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleComingSoon = (feature: string) => {
    import('sonner').then(({ toast }) => toast.info(`${feature} feature is coming soon!`));
  };

  return (
    <PermissionGuard 
      permission={PERMISSIONS.USERS.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don&apos;t have permission to view users.</div>}
    >
      <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif]">
        {/* Top Control Bar (Odoo Style) */}
        <div className="bg-white border-b border-[#e0e0e0] px-4 py-3 flex flex-col gap-3 z-10">
          
          <div className="flex items-center justify-between">
            {/* Action Buttons */}
            <div className="flex items-center gap-2">
              <PermissionGuard permission={PERMISSIONS.USERS.CREATE}>
                <button 
                  onClick={() => setIsInviteModalOpen(true)}
                  className="bg-[#0066cc] text-white px-4 py-2 rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-all flex items-center gap-2"
                >
                  <Plus className="w-4 h-4" />
                  New User
                </button>
              </PermissionGuard>
              <button 
                onClick={() => handleComingSoon('Import')}
                className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-medium hover:bg-[#f8f8f8] transition-colors"
              >
                Import
              </button>
              <button 
                onClick={handleExport}
                className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-medium hover:bg-[#f8f8f8] transition-colors"
              >
                Export
              </button>
              <button 
                onClick={() => handleComingSoon('Bulk Status Change')}
                className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-medium hover:bg-[#f8f8f8] transition-colors"
              >
                Change Status
              </button>
            </div>
            
            {/* Search & Filters */}
            <div className="flex items-center gap-3">
              <form onSubmit={handleSearchSubmit} className="relative">
                <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search users..."
                  className="pl-9 pr-4 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] w-[280px]"
                  value={searchTerm}
                  onChange={handleSearchChange}
                />
              </form>
              <button className="p-2 border border-[#d0d0d0] bg-white rounded-[4px] text-[#898989] hover:bg-[#f0f4ff] hover:text-[#0066cc] hover:border-[#0066cc] transition-colors">
                <SlidersHorizontal className="w-4 h-4" />
              </button>
              <div className="flex items-center border border-[#d0d0d0] rounded-[4px] overflow-hidden">
                <button 
                  onClick={() => setViewMode('list')}
                  className={`p-2 ${viewMode === 'list' ? 'bg-[#f0f4ff] text-[#0066cc]' : 'bg-white text-[#898989] hover:bg-[#f8f8f8]'}`}
                >
                  <LayoutList className="w-4 h-4" />
                </button>
                <button 
                  onClick={() => setViewMode('grid')}
                  className={`p-2 ${viewMode === 'grid' ? 'bg-[#f0f4ff] text-[#0066cc]' : 'bg-white text-[#898989] hover:bg-[#f8f8f8]'}`}
                >
                  <LayoutGrid className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Filter Chips Bar */}
          <div className="flex items-center gap-3 bg-[#f8f8f8] px-3 py-2 rounded-[4px]">
            <div className="flex items-center gap-1 bg-[#e2e8f0] px-2 py-1 rounded text-[13px] font-medium text-[#334155]">
              Active Users
              <button className="text-[#64748b] hover:text-[#0f172a]"><X className="w-3 h-3" /></button>
            </div>
            <div className="flex items-center gap-1 bg-[#e2e8f0] px-2 py-1 rounded text-[13px] font-medium text-[#334155]">
              Group By: Role
              <span className="text-[10px] ml-1">▼</span>
            </div>
            <button className="text-[13px] text-[#0066cc] font-medium hover:underline ml-2">
              Clear All
            </button>
          </div>
        </div>

        {/* Main Content Area - Table */}
        <div className="flex-1 overflow-auto bg-[#f8f8f8] p-4">
          <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
            {viewMode === 'list' ? (
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white border-b border-[#e0e0e0]">
                      <th className="py-3 px-4 w-[40px]">
                        <input type="checkbox" className="w-4 h-4 accent-[#0066cc] border-[#d0d0d0] rounded-sm" />
                      </th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider min-w-[250px]">USER</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">PRIMARY ORGANIZATION</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">ASSIGNED ROLES</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">STATUS</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">LAST LOGIN ACTIVITY</th>
                      <th className="py-3 px-4 w-[50px]"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading && formattedUsers.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-12 text-center text-[#898989]">
                          <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                          Loading users...
                        </td>
                      </tr>
                    ) : formattedUsers.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-12 text-center text-[#898989]">
                          <div className="bg-[#f8f8f8] w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-3">
                            <UserCircle className="w-8 h-8 text-[#d0d0d0]" />
                          </div>
                          <p className="text-[14px] font-medium text-[#242424]">No users found</p>
                          <p className="text-[13px] mt-1">Try adjusting your search or invite a new user.</p>
                        </td>
                      </tr>
                    ) : (
                      formattedUsers.map((user) => (
                        <tr 
                          key={user.id} 
                          onClick={() => navigateToDetails(user.id)}
                          className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group ${user.status === 'Suspended' ? 'bg-[#fff5f5]' : 'bg-white'}`}
                        >
                          <td className="py-3 px-4" onClick={(e) => e.stopPropagation()}>
                            <input type="checkbox" className="w-4 h-4 accent-[#0066cc] border-[#d0d0d0] rounded-sm" />
                          </td>
                          <td className="py-3 px-4">
                            <div className="flex items-center gap-3">
                              <div className="w-9 h-9 rounded-full bg-[#e2e8f0] flex items-center justify-center text-[#0066cc] font-semibold text-[14px]">
                                {user.name.charAt(0).toUpperCase()}
                              </div>
                              <div className="flex flex-col">
                                <span className="text-[14px] font-medium text-[#242424] group-hover:text-[#0066cc] transition-colors flex items-center gap-2">
                                  {user.name}
                                  {user.status === 'Suspended' && (
                                    <span className="bg-[#dc3545] text-white text-[10px] font-bold px-1.5 py-0.5 rounded uppercase">Suspended</span>
                                  )}
                                </span>
                                <span className="text-[12px] text-[#898989]">{user.email}</span>
                              </div>
                            </div>
                          </td>
                          <td className="py-3 px-4">
                            <span className="inline-flex items-center gap-1.5 bg-[#f8f8f8] border border-[#d0d0d0] text-[#242424] px-2 py-1 rounded-[4px] text-[12px] font-medium">
                              <Building2 className="w-3 h-3 text-[#898989]" />
                              {user.primaryOrg}
                            </span>
                          </td>
                          <td className="py-3 px-4">
                            <div className="flex flex-wrap gap-1.5">
                              {user.roles.map((r, i) => (
                                <span key={i} className={`px-2 py-1 rounded-[12px] text-[11px] font-medium whitespace-nowrap ${i === 0 ? 'bg-[#e0f0ff] text-[#0066cc]' : 'bg-[#f1f5f9] text-[#475569]'}`}>
                                  {r}
                                </span>
                              ))}
                            </div>
                          </td>
                          <td className="py-3 px-4 text-center">
                            {user.status === 'Active' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#28a745] ring-2 ring-[#e6f4ea]" title="Active"></span>}
                            {user.status === 'Offline' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#94a3b8] ring-2 ring-[#f1f5f9]" title="Offline"></span>}
                            {user.status === 'Suspended' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#dc3545] ring-2 ring-[#ffe5e5]" title="Suspended"></span>}
                          </td>
                          <td className="py-3 px-4 text-[13px] text-[#64748b]">{user.lastLogin}</td>
                          <td className="py-3 px-4 text-center">
                            <div onClick={(e) => e.stopPropagation()}>
                              <UserActionsDropdown 
                                user={user} 
                                orgId={orgId} 
                                onChangeRole={(u) => setSelectedUserForRole(u)}
                                onRemove={(u) => setSelectedUserForRemove(u)}
                              />
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="p-4 bg-[#f8f8f8] h-full overflow-auto">
                {loading && formattedUsers.length === 0 ? (
                  <div className="py-12 text-center text-[#898989]">
                    <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Loading users...
                  </div>
                ) : formattedUsers.length === 0 ? (
                  <div className="py-12 text-center text-[#898989]">
                    <div className="bg-[#f8f8f8] w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-3">
                      <UserCircle className="w-8 h-8 text-[#d0d0d0]" />
                    </div>
                    <p className="text-[14px] font-medium text-[#242424]">No users found</p>
                    <p className="text-[13px] mt-1">Try adjusting your search or invite a new user.</p>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {formattedUsers.map((user) => (
                      <div 
                        key={user.id}
                        onClick={() => navigateToDetails(user.id)}
                        className="bg-white border border-[#e0e0e0] rounded-[6px] shadow-sm hover:shadow-md hover:border-[#0066cc] transition-all cursor-pointer p-5 flex flex-col gap-4 relative"
                      >
                        <div className="absolute top-4 right-4" onClick={(e) => e.stopPropagation()}>
                          <UserActionsDropdown 
                            user={user} 
                            orgId={orgId} 
                            onChangeRole={(u) => setSelectedUserForRole(u)}
                            onRemove={(u) => setSelectedUserForRemove(u)}
                          />
                        </div>
                        
                        <div className="flex items-center gap-4">
                          <div className="w-12 h-12 rounded bg-[#e2e8f0] flex items-center justify-center text-[#0066cc] font-bold text-[18px]">
                            {user.name.charAt(0).toUpperCase()}
                          </div>
                          <div className="flex flex-col flex-1 truncate">
                            <span className="text-[15px] font-semibold text-[#242424] truncate pr-8">{user.name}</span>
                            <span className="text-[13px] text-[#898989] truncate">{user.email}</span>
                          </div>
                        </div>

                        <div className="flex flex-col gap-2 mt-2">
                          <div className="flex items-center justify-between">
                            <span className="text-[12px] text-[#898989]">Status:</span>
                            <div className="flex items-center gap-1.5">
                              {user.status === 'Active' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#28a745]" title="Active"></span>}
                              {user.status === 'Offline' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#94a3b8]" title="Offline"></span>}
                              {user.status === 'Suspended' && <span className="inline-block w-2.5 h-2.5 rounded-full bg-[#dc3545]" title="Suspended"></span>}
                              <span className="text-[13px] text-[#242424] font-medium">{user.status}</span>
                            </div>
                          </div>
                          
                          <div className="flex flex-col gap-1.5">
                            <span className="text-[12px] text-[#898989]">Roles:</span>
                            <div className="flex flex-wrap gap-1">
                              {user.roles.length > 0 ? user.roles.map((r, i) => (
                                <span key={i} className="px-2 py-0.5 rounded-[12px] text-[11px] font-medium bg-[#f1f5f9] text-[#475569]">
                                  {r}
                                </span>
                              )) : (
                                <span className="text-[12px] text-[#898989] italic">No roles</span>
                              )}
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
            
            {/* Pagination Footer */}
            {totalElements > 0 && (
              <div className="bg-white border-t border-[#e0e0e0] px-4 py-3 flex items-center justify-between text-[13px] text-[#64748b]">
                <span>
                  Showing {((page - 1) * limit) + 1} to {Math.min(page * limit, totalElements)} of {totalElements} entries
                </span>
                <div className="flex items-center gap-1">
                  <button 
                    onClick={() => setPage(p => Math.max(1, p - 1))}
                    disabled={page === 1}
                    className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <div className="flex items-center gap-1">
                    {Array.from({ length: Math.min(totalPages, 5) }).map((_, i) => (
                      <button 
                        key={i} 
                        className={`w-7 h-7 rounded flex items-center justify-center font-medium ${page === i + 1 ? 'bg-[#0066cc] text-white' : 'hover:bg-[#f8f8f8] text-[#242424]'}`}
                        onClick={() => setPage(i + 1)}
                      >
                        {i + 1}
                      </button>
                    ))}
                    {totalPages > 5 && <span className="px-1">...</span>}
                    {totalPages > 5 && (
                      <button className="w-7 h-7 rounded flex items-center justify-center font-medium hover:bg-[#f8f8f8] text-[#242424]">
                        {totalPages}
                      </button>
                    )}
                  </div>
                  <button 
                    onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                    disabled={page === totalPages || totalPages === 0}
                    className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      <InviteUserModal 
        isOpen={isInviteModalOpen} 
        onClose={() => setIsInviteModalOpen(false)} 
        orgId={orgId}
        onSuccess={refresh}
      />

      <ChangeRoleModal
        isOpen={!!selectedUserForRole}
        onClose={() => setSelectedUserForRole(null)}
        orgId={orgId}
        user={selectedUserForRole}
        onSuccess={refresh}
      />

      <RemoveUserModal
        isOpen={!!selectedUserForRemove}
        onClose={() => setSelectedUserForRemove(null)}
        orgId={orgId}
        user={selectedUserForRemove}
        onSuccess={refresh}
      />
    </PermissionGuard>
  );
}
