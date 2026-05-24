"use client";

import React, { useState, use } from "react";
import { ChevronLeft, Check, Camera, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";

export default function UserDetailPage({ params }: { params: Promise<{ orgId: string; userId: string }> }) {
  const unwrappedParams = use(params);
  const orgId = unwrappedParams.orgId;
  const userId = unwrappedParams.userId;
  const router = useRouter();

  const [activeTab, setActiveTab] = useState("access_rights");
  const [status, setStatus] = useState("ACTIVE");

  const [formData, setFormData] = useState({
    name: "Nguyen Van A",
    email: "nguyen.vana@enterprise.com",
    phone: "+84 90 123 4567",
    language: "English (US)",
    timezone: "Asia/Ho_Chi_Minh",
    companies: ["Enterprise Global", "Enterprise APAC"]
  });

  const [accessRights, setAccessRights] = useState({
    sales: "User: Own Documents Only",
    inventory: "Administrator",
    marketing: "None",
    administration: "Access Rights",
    manageAccessRights: true,
    multiOrgAdmin: true,
    apiAccess: false,
    bypass2fa: false
  });

  const handleSave = () => {
    // Implement save logic here
    console.log("Saved", formData, accessRights);
    router.push(`/dashboard/${orgId}/users`);
  };

  const handleDiscard = () => {
    router.push(`/dashboard/${orgId}/users`);
  };

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
              <button 
                onClick={handleSave}
                className="bg-[#0066cc] text-white px-4 py-2 rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-all"
              >
                Save
              </button>
              <button 
                onClick={handleDiscard}
                className="bg-white border border-[#d0d0d0] text-[#242424] px-4 py-2 rounded-[4px] text-[14px] font-medium hover:bg-[#f8f8f8] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] transition-colors"
              >
                Discard
              </button>
            </div>

            {/* Status Badges */}
            <div className="flex items-center gap-2 font-bold text-[11px] uppercase tracking-wider">
              <button 
                onClick={() => setStatus('DRAFT')}
                className={`px-3 py-1.5 rounded-[4px] ${status === 'DRAFT' ? 'bg-[#6c757d] text-white' : 'text-[#898989] hover:bg-[#f0f4ff]'}`}
              >
                Draft
              </button>
              <button 
                onClick={() => setStatus('ACTIVE')}
                className={`px-3 py-1.5 rounded-[4px] ${status === 'ACTIVE' ? 'bg-[#28a745] text-white' : 'text-[#898989] hover:bg-[#f0f4ff]'}`}
              >
                Active
              </button>
              <button 
                onClick={() => setStatus('INACTIVE')}
                className={`px-3 py-1.5 rounded-[4px] ${status === 'INACTIVE' ? 'bg-[#dc3545] text-white' : 'text-[#898989] hover:bg-[#f0f4ff]'}`}
              >
                Inactive
              </button>
            </div>
          </div>
        </div>

        {/* Form Container */}
        <div className="p-6 max-w-[1000px] mx-auto w-full">
          <div className="bg-white border border-[#e0e0e0] rounded-[8px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] p-8">
            
            {/* Header Section */}
            <div className="flex gap-6 items-start border-b border-[#e0e0e0] pb-8 mb-8">
              <div className="relative group cursor-pointer w-[100px] h-[100px] border border-[#d0d0d0] rounded bg-[#f8f8f8] overflow-hidden">
                <img 
                  src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix&backgroundColor=e2e8f0" 
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
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="text-[32px] font-bold text-[#242424] w-full border-b border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] focus:outline-none mb-1 bg-transparent px-0 py-1"
                  placeholder="e.g. John Doe"
                />
                <p className="text-[14px] text-[#898989] font-medium">Edit User</p>
              </div>
            </div>

            {/* General Information Grid */}
            <div className="grid grid-cols-2 gap-x-12 gap-y-6 mb-8">
              <div className="flex flex-col gap-1">
                <label className="text-[14px] font-bold text-[#242424]">Email Address</label>
                <input 
                  type="email" 
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="px-0 py-1 border-b border-[#d0d0d0] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc] bg-transparent"
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

              <div className="flex flex-col gap-1 col-span-2">
                <label className="text-[14px] font-bold text-[#242424]">Allowed Companies</label>
                <div className="flex flex-wrap items-center gap-2 mt-1 border-b border-[#d0d0d0] pb-1">
                  {formData.companies.map((company, i) => (
                    <div key={i} className="flex items-center gap-1 bg-[#f1f5f9] px-2 py-1 rounded-[4px] text-[13px] text-[#242424]">
                      {company}
                      <button className="text-[#898989] hover:text-[#dc3545]"><X className="w-3 h-3" /></button>
                    </div>
                  ))}
                  <input 
                    type="text" 
                    placeholder="Add organization..."
                    className="flex-1 min-w-[150px] text-[14px] focus:outline-none bg-transparent placeholder-[#898989]"
                  />
                </div>
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
                
                {/* Application Access */}
                <div className="flex flex-col gap-4">
                  <h3 className="text-[12px] font-bold text-[#242424] uppercase tracking-wider mb-2">Application Access</h3>
                  
                  <div className="grid grid-cols-[1fr_2fr] items-center gap-4">
                    <label className="text-[14px] font-medium text-[#242424]">Sales</label>
                    <select 
                      value={accessRights.sales}
                      onChange={(e) => setAccessRights({ ...accessRights, sales: e.target.value })}
                      className="w-full px-3 py-1.5 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option>None</option>
                      <option>User: Own Documents Only</option>
                      <option>User: All Documents</option>
                      <option>Administrator</option>
                    </select>
                  </div>

                  <div className="grid grid-cols-[1fr_2fr] items-center gap-4">
                    <label className="text-[14px] font-medium text-[#242424]">Inventory</label>
                    <select 
                      value={accessRights.inventory}
                      onChange={(e) => setAccessRights({ ...accessRights, inventory: e.target.value })}
                      className="w-full px-3 py-1.5 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option>None</option>
                      <option>User</option>
                      <option>Administrator</option>
                    </select>
                  </div>

                  <div className="grid grid-cols-[1fr_2fr] items-center gap-4">
                    <label className="text-[14px] font-medium text-[#242424]">Marketing</label>
                    <select 
                      value={accessRights.marketing}
                      onChange={(e) => setAccessRights({ ...accessRights, marketing: e.target.value })}
                      className="w-full px-3 py-1.5 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option>None</option>
                      <option>User</option>
                      <option>Manager</option>
                    </select>
                  </div>

                  <div className="grid grid-cols-[1fr_2fr] items-center gap-4">
                    <label className="text-[14px] font-medium text-[#242424]">Administration</label>
                    <select 
                      value={accessRights.administration}
                      onChange={(e) => setAccessRights({ ...accessRights, administration: e.target.value })}
                      className="w-full px-3 py-1.5 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option>None</option>
                      <option>Access Rights</option>
                      <option>Settings</option>
                    </select>
                  </div>
                </div>

                {/* Technical Settings */}
                <div className="flex flex-col gap-4">
                  <h3 className="text-[12px] font-bold text-[#242424] uppercase tracking-wider mb-2">Technical Settings</h3>
                  
                  <label className="flex items-center gap-3 cursor-pointer group">
                    <div className={`w-[18px] h-[18px] rounded-[3px] border flex items-center justify-center transition-colors ${accessRights.manageAccessRights ? 'bg-[#0066cc] border-[#0066cc]' : 'bg-white border-[#d0d0d0] group-hover:border-[#0066cc]'}`}>
                      {accessRights.manageAccessRights && <Check className="w-3.5 h-3.5 text-white" />}
                    </div>
                    <span className="text-[14px] text-[#242424]">Manage Access Rights</span>
                    <input 
                      type="checkbox" 
                      className="hidden" 
                      checked={accessRights.manageAccessRights}
                      onChange={(e) => setAccessRights({ ...accessRights, manageAccessRights: e.target.checked })}
                    />
                  </label>

                  <label className="flex items-center gap-3 cursor-pointer group">
                    <div className={`w-[18px] h-[18px] rounded-[3px] border flex items-center justify-center transition-colors ${accessRights.multiOrgAdmin ? 'bg-[#0066cc] border-[#0066cc]' : 'bg-white border-[#d0d0d0] group-hover:border-[#0066cc]'}`}>
                      {accessRights.multiOrgAdmin && <Check className="w-3.5 h-3.5 text-white" />}
                    </div>
                    <span className="text-[14px] text-[#242424]">Multi-Organizations Admin</span>
                    <input 
                      type="checkbox" 
                      className="hidden" 
                      checked={accessRights.multiOrgAdmin}
                      onChange={(e) => setAccessRights({ ...accessRights, multiOrgAdmin: e.target.checked })}
                    />
                  </label>

                  <label className="flex items-center gap-3 cursor-pointer group">
                    <div className={`w-[18px] h-[18px] rounded-[3px] border flex items-center justify-center transition-colors ${accessRights.apiAccess ? 'bg-[#0066cc] border-[#0066cc]' : 'bg-white border-[#d0d0d0] group-hover:border-[#0066cc]'}`}>
                      {accessRights.apiAccess && <Check className="w-3.5 h-3.5 text-white" />}
                    </div>
                    <span className="text-[14px] text-[#242424]">API Access Allowed</span>
                    <input 
                      type="checkbox" 
                      className="hidden" 
                      checked={accessRights.apiAccess}
                      onChange={(e) => setAccessRights({ ...accessRights, apiAccess: e.target.checked })}
                    />
                  </label>

                  <label className="flex items-center gap-3 cursor-pointer group">
                    <div className={`w-[18px] h-[18px] rounded-[3px] border flex items-center justify-center transition-colors ${accessRights.bypass2fa ? 'bg-[#0066cc] border-[#0066cc]' : 'bg-white border-[#d0d0d0] group-hover:border-[#0066cc]'}`}>
                      {accessRights.bypass2fa && <Check className="w-3.5 h-3.5 text-white" />}
                    </div>
                    <span className="text-[14px] text-[#242424]">Bypass Two-Factor Auth (Internal Network)</span>
                    <input 
                      type="checkbox" 
                      className="hidden" 
                      checked={accessRights.bypass2fa}
                      onChange={(e) => setAccessRights({ ...accessRights, bypass2fa: e.target.checked })}
                    />
                  </label>
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
