"use client";

import React, { useState, useMemo, useEffect } from "react";
import { Search, Plus, ChevronLeft, ChevronRight, UserCircle, RefreshCcw, SlidersHorizontal, LayoutList, LayoutGrid, Building2, X, Send } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import { useOrganizationMembers } from "@/features/organization/hooks/useOrganizationMembers";
import { InviteUserModal } from "@/features/organization/components/InviteUserModal";
import { BulkInviteUserModal } from "@/features/organization/components/BulkInviteUserModal";
import { UserActionsDropdown } from "@/features/organization/components/UserActionsDropdown";
import { ChangeRoleModal } from "@/features/organization/components/ChangeRoleModal";
import { RemoveUserModal } from "@/features/organization/components/RemoveUserModal";
import { getInvitationsApi, resendInvitationApi, OrganizationInvitationResponse } from "@/features/organization/services/invitationService";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";

export function UsersTab({ orgId }: { orgId: string }) {
  const [searchInputValue, setSearchInputValue] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [isBulkInviteModalOpen, setIsBulkInviteModalOpen] = useState(false);
  const [selectedUserForRole, setSelectedUserForRole] = useState<any>(null);
  const [selectedUserForRemove, setSelectedUserForRemove] = useState<any>(null);
  const [page, setPage] = useState(1);
  const [viewMode, setViewMode] = useState<'list' | 'grid'>('list');
  const limit = 10;
  
  const [activeSubTab, setActiveSubTab] = useState<'active' | 'pending'>('active');
  const [invitations, setInvitations] = useState<OrganizationInvitationResponse[]>([]);
  const [loadingInvitations, setLoadingInvitations] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchInputValue);
      setPage(1);
    }, 400);
    return () => clearTimeout(timer);
  }, [searchInputValue]);

  const { members, loading, totalElements, totalPages, refresh } = useOrganizationMembers({
    organizationId: orgId,
    query: debouncedSearch,
    page: page,
    limit,
  });

  const fetchInvitations = async () => {
    setLoadingInvitations(true);
    try {
      const data = await getInvitationsApi(orgId);
      setInvitations(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingInvitations(false);
    }
  };

  useEffect(() => {
    if (activeSubTab === 'pending') {
      fetchInvitations();
    }
  }, [activeSubTab, orgId]);

  const handleResendInvitation = async (invitationId: string) => {
    try {
      await resendInvitationApi(orgId, invitationId);
      toast.success("Invitation resent successfully!");
      fetchInvitations();
    } catch (error) {
      toast.error("Failed to resend invitation.");
    }
  };

  const formattedUsers = useMemo(() => {
    return members.map((u) => ({
      id: u.id,
      name: `${u.firstName || ''} ${u.lastName || ''}`.trim() || 'Unknown User',
      email: u.email,
      roles: u.roles?.map(r => r.name) || [],
      roleIds: u.roles?.map(r => r.id) || [],
      status: u.status || 'Active',
      lastLogin: u.lastLogin || '-',
    }));
  }, [members]);

  const handleExport = () => {
    const csvContent = [
      ['Name', 'Email', 'Roles', 'Status', 'Last Login'],
      ...formattedUsers.map(u => [
        `"${u.name}"`,
        `"${u.email}"`,
        `"${u.roles.join(', ')}"`,
        `"${u.status}"`,
        `"${u.lastLogin}"`
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
    <div className="h-full bg-white flex flex-col font-['Segoe_UI',_sans-serif] border border-[#e0e0e0] rounded-[8px] overflow-hidden shadow-[0px_1px_3px_rgba(0,0,0,0.05)]">
      {/* Top Control Bar */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 py-4 flex flex-col gap-4 z-10">
        <div className="flex items-center justify-between">
          {/* Action Buttons */}
          <div className="flex items-center gap-3">
            <PermissionGuard permission={PERMISSIONS.USERS.CREATE}>
              <button 
                onClick={() => setIsBulkInviteModalOpen(true)}
                className="bg-white border border-[#0066cc] text-[#0066cc] px-4 py-2 rounded-[4px] text-[14px] font-[600] hover:bg-[#f0f4ff] transition-all"
              >
                Import Users
              </button>
            </PermissionGuard>
            <button 
              onClick={handleExport}
              className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-[500] hover:bg-[#f8f8f8] transition-colors"
            >
              Export
            </button>
          </div>
          
          {/* Search & Filters */}
          <div className="flex items-center gap-3">
            <div className="relative">
              <Search className="w-4 h-4 text-[#898989] absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search users..."
                className="pl-9 pr-4 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] w-[280px]"
                value={searchInputValue}
                onChange={(e) => setSearchInputValue(e.target.value)}
              />
            </div>
            {activeSubTab === 'active' && (
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
            )}
          </div>
        </div>

        {/* Sub Tabs */}
        <div className="flex items-center gap-6 border-b border-[#e0e0e0] mt-2">
          <button 
            onClick={() => setActiveSubTab('active')}
            className={`pb-3 text-[14px] font-[600] transition-colors relative ${activeSubTab === 'active' ? 'text-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
          >
            Active Members
            {activeSubTab === 'active' && <div className="absolute bottom-[-1px] left-0 right-0 h-0.5 bg-[#0066cc]"></div>}
          </button>
          <button 
            onClick={() => setActiveSubTab('pending')}
            className={`pb-3 text-[14px] font-[600] transition-colors relative ${activeSubTab === 'pending' ? 'text-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
          >
            Invitations
            {activeSubTab === 'pending' && <div className="absolute bottom-[-1px] left-0 right-0 h-0.5 bg-[#0066cc]"></div>}
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
        {activeSubTab === 'active' ? (
          <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col h-full">
            {viewMode === 'list' ? (
              <div className="overflow-x-auto flex-1">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider min-w-[250px]">USER DETAILS</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">ROLE IN ORG</th>
                      <th className="py-3 px-4 w-[50px]"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading && formattedUsers.length === 0 ? (
                      <tr>
                        <td colSpan={3} className="py-12 text-center text-[#898989]">
                          <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                          Loading users...
                        </td>
                      </tr>
                    ) : formattedUsers.length === 0 ? (
                      <tr>
                        <td colSpan={3} className="py-12 text-center text-[#898989]">
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
                          onClick={() => setSelectedUserForRole(user)}
                          className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group ${user.status === 'Suspended' ? 'bg-[#fff5f5]' : 'bg-white'}`}
                        >
                          <td className="py-3 px-4">
                            <div className="flex items-center gap-3">
                              <div className="w-9 h-9 rounded-full bg-[#e2e8f0] flex items-center justify-center text-[#0066cc] font-semibold text-[14px]">
                                {user.name.charAt(0).toUpperCase()}
                              </div>
                              <div className="flex flex-col">
                                <span className="text-[14px] font-[600] text-[#242424] flex items-center gap-2">
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
                            <div className="flex flex-wrap gap-1.5">
                              {user.roles.map((r, i) => (
                                <span key={i} className={`px-2 py-1 rounded-[12px] text-[11px] font-[600] whitespace-nowrap border ${i === 0 ? 'bg-[#e0f0ff] text-[#0066cc] border-[#b3d4ff]' : 'bg-[#f8f8f8] text-[#242424] border-[#e0e0e0]'}`}>
                                  {r}
                                </span>
                              ))}
                            </div>
                          </td>
                          <td className="py-3 px-4 text-center" onClick={(e) => e.stopPropagation()}>
                            <UserActionsDropdown 
                              user={user} 
                              orgId={orgId} 
                              onChangeRole={(u) => setSelectedUserForRole(u)}
                              onRemove={(u) => setSelectedUserForRemove(u)}
                            />
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="p-4 bg-white h-full overflow-auto flex-1">
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
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {formattedUsers.map((user) => (
                      <div 
                        key={user.id}
                        onClick={() => setSelectedUserForRole(user)}
                        className="bg-white border border-[#e0e0e0] rounded-[6px] shadow-sm hover:shadow-md hover:border-[#0066cc] transition-all p-5 flex flex-col gap-4 relative cursor-pointer"
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
                            <span className="text-[15px] font-[600] text-[#242424] truncate pr-8">{user.name}</span>
                            <span className="text-[13px] text-[#898989] truncate">{user.email}</span>
                          </div>
                        </div>

                        <div className="flex flex-col gap-2 mt-2">
                          <div className="flex flex-col gap-1.5">
                            <span className="text-[12px] text-[#898989]">Roles:</span>
                            <div className="flex flex-wrap gap-1">
                              {user.roles.length > 0 ? user.roles.map((r, i) => (
                                <span key={i} className="px-2 py-0.5 rounded-[12px] text-[11px] font-[600] bg-[#f8f8f8] border border-[#e0e0e0] text-[#242424]">
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
              <div className="bg-[#f8f8f8] border-t border-[#e0e0e0] px-4 py-3 flex items-center justify-between text-[13px] text-[#64748b]">
                <span>
                  Showing {((page - 1) * limit) + 1} to {Math.min(page * limit, totalElements)} of {totalElements} entries
                </span>
                <div className="flex items-center gap-1">
                  <button 
                    onClick={() => setPage(p => Math.max(1, p - 1))}
                    disabled={page === 1}
                    className="p-1 hover:bg-[#e0e0e0] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4" />
                  </button>
                  <div className="flex items-center gap-1">
                    {Array.from({ length: Math.min(totalPages, 5) }).map((_, i) => (
                      <button 
                        key={i} 
                        className={`w-7 h-7 rounded flex items-center justify-center font-[600] transition-colors ${page === i + 1 ? 'bg-[#0066cc] text-white' : 'hover:bg-[#e0e0e0] text-[#242424]'}`}
                        onClick={() => setPage(i + 1)}
                      >
                        {i + 1}
                      </button>
                    ))}
                    {totalPages > 5 && <span className="px-1">...</span>}
                    {totalPages > 5 && (
                      <button className="w-7 h-7 rounded flex items-center justify-center font-[600] hover:bg-[#e0e0e0] text-[#242424]">
                        {totalPages}
                      </button>
                    )}
                  </div>
                  <button 
                    onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                    disabled={page === totalPages || totalPages === 0}
                    className="p-1 hover:bg-[#e0e0e0] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
                  >
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          /* Pending Invitations View */
          <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col h-full">
            <div className="overflow-x-auto flex-1">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                    <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">EMAIL</th>
                    <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">STATUS</th>
                    <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">CREATED AT</th>
                    <th className="py-3 px-4 w-[100px] text-center">ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {loadingInvitations && invitations.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="py-12 text-center text-[#898989]">
                        <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                        Loading invitations...
                      </td>
                    </tr>
                  ) : invitations.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="py-12 text-center text-[#898989]">
                        <div className="bg-[#f8f8f8] w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-3">
                          <Send className="w-8 h-8 text-[#d0d0d0]" />
                        </div>
                        <p className="text-[14px] font-medium text-[#242424]">No pending invitations</p>
                        <p className="text-[13px] mt-1">New invitations will appear here until they are accepted.</p>
                      </td>
                    </tr>
                  ) : (
                    invitations.map((inv) => (
                      <tr 
                        key={inv.id} 
                        className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors"
                      >
                        <td className="py-3 px-4 text-[14px] font-[600] text-[#242424]">
                          {inv.email}
                        </td>
                        <td className="py-3 px-4">
                          <span className={`px-2 py-1 rounded-[4px] text-[11px] font-[600] uppercase tracking-wider ${
                            inv.status === 'PENDING' ? 'bg-[#fff3cd] text-[#856404]' : 
                            inv.status === 'EXPIRED' ? 'bg-[#f8d7da] text-[#721c24]' : 
                            'bg-[#e2e3e5] text-[#383d41]'
                          }`}>
                            {inv.status}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-[13px] text-[#64748b]">
                          {new Date(inv.createdAt).toLocaleDateString()}
                        </td>
                        <td className="py-3 px-4 text-center">
                          {(inv.status === 'PENDING' || inv.status === 'EXPIRED') && (
                            <Button 
                              variant="ghost" 
                              size="sm" 
                              onClick={() => handleResendInvitation(inv.id)}
                              className="text-[#0066cc] hover:text-[#004499] hover:bg-[#e0f0ff]"
                            >
                              Resend
                            </Button>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      <InviteUserModal 
        isOpen={isInviteModalOpen} 
        onClose={() => setIsInviteModalOpen(false)} 
        orgId={orgId}
        onSuccess={() => { refresh(); fetchInvitations(); }}
      />

      <BulkInviteUserModal
        isOpen={isBulkInviteModalOpen}
        onClose={() => setIsBulkInviteModalOpen(false)}
        orgId={orgId}
        onSuccess={() => { refresh(); fetchInvitations(); }}
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
    </div>
  );
}
