"use client";

import React, { useState, useEffect, use } from "react";
import { Camera, Check, Loader2 } from "lucide-react";
import { useRouter } from "next/navigation";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import { useOrganizationMember } from "@/features/organization/hooks/useOrganizationMember";
import { useRoles } from "@/features/organization/hooks/useRoles";

export default function UserDetailPage({ params }: { params: Promise<{ orgId: string; userId: string }> }) {
  const unwrappedParams = use(params);
  const orgId = unwrappedParams.orgId;
  const userId = unwrappedParams.userId;
  const router = useRouter();

  const { member, loading, saving, updateRoles } = useOrganizationMember(orgId, userId);
  const { roles: availableRoles, loading: rolesLoading } = useRoles(orgId);

  const [activeTab, setActiveTab] = useState("access_rights");
  const [status, setStatus] = useState("ACTIVE");

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    language: "English (US)",
    timezone: "Asia/Ho_Chi_Minh",
    companies: ["Current Organization"]
  });

  const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([]);

  useEffect(() => {
    if (member) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setFormData(prev => ({
        ...prev,
        name: `${member.firstName || ''} ${member.lastName || ''}`.trim(),
        email: member.email || "",
      }));
      setStatus(member.status || "ACTIVE");
      setSelectedRoleIds(member.roles?.map(r => r.id) || []);
    }
  }, [member]);

  const handleSave = async () => {
    // Current backend API only supports updating roles through this endpoint for admins.
    // Updating profile (name, phone) is restricted to the user themselves.
    const success = await updateRoles(selectedRoleIds);
    if (success) {
      router.push(`/dashboard/${orgId}/users`);
    }
  };

  const handleDiscard = () => {
    router.push(`/dashboard/${orgId}/users`);
  };

  const toggleRole = (roleId: string) => {
    setSelectedRoleIds(prev => 
      prev.includes(roleId) ? prev.filter(id => id !== roleId) : [...prev, roleId]
    );
  };

  if (loading || !member) {
    return (
      <div className="h-full flex items-center justify-center bg-[#f8f8f8]">
        <Loader2 className="w-8 h-8 text-[#0066cc] animate-spin" />
      </div>
    );
  }

  return (
    <PermissionGuard 
      permission={PERMISSIONS.USERS.READ} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied.</div>}
    >
      <div className="h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif] overflow-auto">
        
        {/* Top Control Bar (Odoo Style) */}
        <div className="bg-white border-b border-[#e0e0e0] px-6 py-4 sticky top-0 z-10 shadow-[0px_1px_2px_rgba(0,0,0,0.05)]">
          <div className="flex items-center gap-2 mb-4 text-[13px]">
            <span className="text-[#898989] cursor-pointer hover:underline" onClick={() => router.push(`/dashboard/${orgId}`)}>Dashboard</span>
            <span className="text-[#898989]">{'>'}</span>
            <span className="text-[#898989] cursor-pointer hover:underline" onClick={() => router.push(`/dashboard/${orgId}/users`)}>Users</span>
            <span className="text-[#898989]">{'>'}</span>
            <span className="text-[#0066cc] font-medium">{formData.name}</span>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <PermissionGuard permission={PERMISSIONS.USERS.WRITE} fallback={<div></div>}>
                <button 
                  onClick={handleSave}
                  disabled={saving}
                  className="bg-[#0066cc] text-white px-4 py-2 rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] disabled:opacity-50 transition-all flex items-center gap-2"
                >
                  {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                  Save
                </button>
              </PermissionGuard>
              <button 
                onClick={handleDiscard}
                className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-medium hover:bg-[#f8f8f8] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] transition-colors"
              >
                Discard
              </button>
            </div>

            {/* Status Badges */}
            <div className="flex items-center gap-2 font-bold text-[11px] uppercase tracking-wider">
              <div className={`px-3 py-1.5 rounded-[4px] ${status === 'DRAFT' ? 'bg-[#6c757d] text-white' : 'text-[#898989]'}`}>
                Draft
              </div>
              <div className={`px-3 py-1.5 rounded-[4px] ${status === 'Active' || status === 'ACTIVE' ? 'bg-[#28a745] text-white' : 'text-[#898989]'}`}>
                Active
              </div>
              <div className={`px-3 py-1.5 rounded-[4px] ${status === 'INACTIVE' || status === 'Suspended' ? 'bg-[#dc3545] text-white' : 'text-[#898989]'}`}>
                Inactive
              </div>
            </div>
          </div>
        </div>

        {/* Form Container */}
        <div className="p-6 max-w-[1000px] mx-auto w-full">
          <div className="bg-white border border-[#e0e0e0] rounded-[8px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] p-8">
            
            {/* Header Section */}
            <div className="flex gap-6 items-start border-b border-[#e0e0e0] pb-8 mb-8">
              <div className="relative group cursor-pointer w-[100px] h-[100px] border border-[#d0d0d0] rounded bg-[#f8f8f8] overflow-hidden">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img 
                  src={`https://api.dicebear.com/7.x/initials/svg?seed=${formData.name}&backgroundColor=e2e8f0&textColor=0066cc`} 
                  alt="Avatar" 
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <Camera className="w-6 h-6 text-white" />
                </div>
              </div>

              <div className="flex-1">
                <input 
                  type="text" 
                  value={formData.name}
                  readOnly
                  className="text-[32px] font-bold text-[#242424] w-full border-b border-transparent focus:outline-none mb-1 bg-transparent px-0 py-1"
                />
                <p className="text-[14px] text-[#898989] font-medium">Organization Member</p>
              </div>
            </div>

            {/* General Information Grid */}
            <div className="grid grid-cols-2 gap-x-12 gap-y-6 mb-8">
              <div className="flex flex-col gap-1">
                <label className="text-[14px] font-bold text-[#242424]">Email Address</label>
                <input 
                  type="email" 
                  value={formData.email}
                  readOnly
                  className="px-0 py-1 border-b border-transparent text-[14px] text-[#242424] focus:outline-none bg-transparent"
                />
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-[14px] font-bold text-[#242424]">Phone</label>
                <input 
                  type="text" 
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  className="px-0 py-1 border-b border-[#d0d0d0] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc] bg-transparent"
                />
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-[14px] font-bold text-[#242424]">Language</label>
                <select 
                  value={formData.language}
                  onChange={(e) => setFormData({ ...formData, language: e.target.value })}
                  className="px-0 py-1 border-b border-[#d0d0d0] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc] bg-transparent cursor-pointer"
                >
                  <option>English (US)</option>
                  <option>Vietnamese (VN)</option>
                  <option>French (FR)</option>
                </select>
              </div>
              <div className="flex flex-col gap-1">
                <label className="text-[14px] font-bold text-[#242424]">Timezone</label>
                <select 
                  value={formData.timezone}
                  onChange={(e) => setFormData({ ...formData, timezone: e.target.value })}
                  className="px-0 py-1 border-b border-[#d0d0d0] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc] bg-transparent cursor-pointer"
                >
                  <option>Asia/Ho_Chi_Minh</option>
                  <option>America/New_York</option>
                  <option>Europe/London</option>
                </select>
              </div>
            </div>

            {/* Tabs */}
            <div className="flex gap-6 border-b border-[#e0e0e0] mb-6">
              <button 
                onClick={() => setActiveTab('access_rights')}
                className={`pb-3 text-[14px] font-bold ${activeTab === 'access_rights' ? 'text-[#0066cc] border-b-2 border-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
              >
                Access Rights
              </button>
              <button 
                onClick={() => setActiveTab('preferences')}
                className={`pb-3 text-[14px] font-bold ${activeTab === 'preferences' ? 'text-[#0066cc] border-b-2 border-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
              >
                Preferences
              </button>
            </div>

            {/* Tab Content */}
            {activeTab === 'access_rights' && (
              <div className="grid grid-cols-2 gap-12">
                
                {/* Organization Roles Mapping */}
                <div className="flex flex-col gap-4 col-span-2 md:col-span-1">
                  <h3 className="text-[12px] font-bold text-[#242424] uppercase tracking-wider mb-2">Organization Roles</h3>
                  
                  {rolesLoading ? (
                    <div className="text-[13px] text-[#898989] flex items-center gap-2">
                      <Loader2 className="w-4 h-4 animate-spin" /> Loading roles...
                    </div>
                  ) : availableRoles.length === 0 ? (
                    <div className="text-[13px] text-[#898989]">No roles available in this organization.</div>
                  ) : (
                    <div className="flex flex-col gap-3">
                      {availableRoles.map(role => (
                        <label key={role.id} className="flex items-center gap-3 cursor-pointer group">
                          <div className={`w-[18px] h-[18px] rounded-[3px] border flex items-center justify-center transition-colors ${selectedRoleIds.includes(role.id) ? 'bg-[#0066cc] border-[#0066cc]' : 'bg-white border-[#d0d0d0] group-hover:border-[#0066cc]'}`}>
                            {selectedRoleIds.includes(role.id) && <Check className="w-3.5 h-3.5 text-white" />}
                          </div>
                          <div className="flex flex-col">
                            <span className="text-[14px] font-medium text-[#242424]">{role.name}</span>
                            {/* {role.description && <span className="text-[12px] text-[#898989]">{role.description}</span>} */}
                          </div>
                          <input 
                            type="checkbox" 
                            className="hidden" 
                            checked={selectedRoleIds.includes(role.id)}
                            onChange={() => toggleRole(role.id)}
                          />
                        </label>
                      ))}
                    </div>
                  )}
                </div>

                {/* Info Block instead of Technical Settings */}
                <div className="flex flex-col gap-4 col-span-2 md:col-span-1 bg-[#f8f8f8] p-4 rounded-[4px] border border-[#e0e0e0]">
                  <h3 className="text-[12px] font-bold text-[#242424] uppercase tracking-wider mb-2">Note on Access Rights</h3>
                  <p className="text-[13px] text-[#64748b] leading-relaxed">
                    Roles define what modules and features this user can access. A user can have multiple roles, and their permissions will be cumulative.
                  </p>
                  <p className="text-[13px] text-[#64748b] leading-relaxed">
                    Changes to roles are applied immediately upon saving, but the user may need to refresh their application to see new menu items.
                  </p>
                </div>

              </div>
            )}

            {activeTab === 'preferences' && (
              <div className="py-8 text-center text-[#898989]">
                <p>Preferences configuration options will appear here.</p>
              </div>
            )}

          </div>
        </div>
      </div>
    </PermissionGuard>
  );
}
