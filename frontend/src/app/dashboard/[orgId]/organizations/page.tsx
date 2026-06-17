"use client";

import React, { useState, useEffect, use } from "react";
import { Building2, Save, Info, Users, ShieldCheck } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import { useOrganizations } from "@/features/organization/hooks/useOrganizations";
import { usePermissions } from "@/hooks/use-permissions";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { AddressInput } from "@/components/ui/address-input";
import { Textarea } from "@/components/ui/textarea";
import { UsersTab } from "@/features/organization/components/UsersTab";
import { RolesTab } from "@/features/organization/components/RolesTab";
import { useSearchParams, useRouter } from "next/navigation";

export default function OrganizationsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const unwrappedParams = use(params);
  const orgId = unwrappedParams.orgId;
  const router = useRouter();
  const searchParams = useSearchParams();
  const defaultTab = searchParams.get('tab') === 'roles' ? 'roles' : 'users';

  const { organizations, updateOrganization } = useOrganizations();
  const { hasPermission } = usePermissions();
  
  const [activeTab, setActiveTab] = useState<'users' | 'roles'>(defaultTab);

  const currentOrg = organizations.find(o => o.id === orgId);

  // Form Fields
  const [formName, setFormName] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formAddress, setFormAddress] = useState("");
  const [formHotline, setFormHotline] = useState("");
  const [formTaxCode, setFormTaxCode] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (currentOrg) {
      setFormName(currentOrg.name || "");
      setFormDescription(currentOrg.description || "");
      setFormAddress(currentOrg.address || "");
      setFormHotline(currentOrg.hotline || "");
      setFormTaxCode(currentOrg.taxCode || "");
    }
  }, [currentOrg]);

  useEffect(() => {
    if (searchParams.get('tab') === 'roles') {
      setActiveTab('roles');
    } else if (searchParams.get('tab') === 'users') {
      setActiveTab('users');
    }
  }, [searchParams]);

  const handleTabChange = (tab: 'users' | 'roles') => {
    setActiveTab(tab);
    router.replace(`/dashboard/${orgId}/organizations?tab=${tab}`);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formName.trim()) return toast.error("Organization name is required.");
    if (!formAddress.trim()) return toast.error("Address is required.");
    if (!formHotline.trim()) return toast.error("Hotline number is required.");
    if (!formTaxCode.trim()) return toast.error("Tax code is required.");

    const payload = {
      name: formName.trim(),
      description: formDescription.trim(),
      address: formAddress.trim(),
      hotline: formHotline.trim(),
      taxCode: formTaxCode.trim(),
    };

    setIsSaving(true);
    try {
      const success = await updateOrganization(orgId, payload);
      if (success) {
        toast.success("Organization details updated successfully!");
      }
    } catch (err) {
      console.error(err);
      toast.error("Failed to save organization. Please check details.");
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentOrg) {
    return (
      <div className="flex flex-col justify-center items-center h-full text-[#898989]">
        <Building2 className="w-12 h-12 text-[#d0d0d0] mb-3" />
        <span className="text-[14px]">Loading organization details...</span>
      </div>
    );
  }

  const canEdit = hasPermission(PERMISSIONS.ORGANIZATIONS.WRITE);

  return (
    <div className="h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif] overflow-hidden">
      {/* Top Header */}
      <div className="bg-white border-b border-[#e0e0e0] px-8 py-5 flex items-center justify-between shrink-0 shadow-[0px_1px_2px_rgba(0,0,0,0.05)] z-10">
        <div>
          <div className="flex items-center text-[12px] text-[#898989] font-[600] tracking-wide uppercase mb-1">
            <span>Administration</span>
            <span className="mx-2">/</span>
            <span className="text-[#0066cc] font-[600]">Organization Settings</span>
          </div>
          <h1 className="text-[28px] font-bold text-[#242424] leading-tight">
            {currentOrg.name}
          </h1>
        </div>
        
        {/* Module Tabs (Right aligned) */}
        <div className="flex items-center bg-[#f8f8f8] p-1 rounded-[6px] border border-[#e0e0e0]">
          <button 
            onClick={() => handleTabChange('users')}
            className={`flex items-center gap-2 px-6 py-2 rounded-[4px] text-[14px] font-[600] transition-colors ${activeTab === 'users' ? 'bg-white shadow-sm text-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
          >
            <Users className="w-4 h-4" />
            Users
          </button>
          <button 
            onClick={() => handleTabChange('roles')}
            className={`flex items-center gap-2 px-6 py-2 rounded-[4px] text-[14px] font-[600] transition-colors ${activeTab === 'roles' ? 'bg-white shadow-sm text-[#0066cc]' : 'text-[#898989] hover:text-[#242424]'}`}
          >
            <ShieldCheck className="w-4 h-4" />
            Roles & Permissions
          </button>
        </div>
      </div>

      {/* Main Content Split View */}
      <div className="flex-1 overflow-hidden flex flex-col lg:flex-row p-6 gap-6 max-w-[1600px] mx-auto w-full">
        
        {/* Left Side: Organization Form */}
        <div className="w-full lg:w-[400px] flex flex-col gap-4 overflow-y-auto pr-2 shrink-0 h-full">
          <form 
            onSubmit={handleSave}
            className="bg-white border border-[#e0e0e0] rounded-[8px] shadow-[0px_1px_3px_rgba(0,0,0,0.05)] flex flex-col h-full"
          >
            <div className="px-5 py-4 border-b border-[#e0e0e0] bg-[#f8f8f8] flex items-center gap-2">
              <Building2 className="w-5 h-5 text-[#0066cc]" />
              <h2 className="text-[16px] font-bold text-[#242424]">
                Organization Profile
              </h2>
            </div>

            <div className="p-5 flex-1 overflow-y-auto space-y-5">
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">
                  Organization Name <span className="text-[#dc3545]">*</span>
                </label>
                <Input 
                  value={formName}
                  onChange={e => setFormName(e.target.value)}
                  placeholder="e.g. Công ty Cổ phần Công nghệ DUT"
                  required
                  disabled={!canEdit}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] text-[14px]"
                />
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">
                  Tax Code <span className="text-[#dc3545]">*</span>
                </label>
                <Input 
                  value={formTaxCode}
                  onChange={e => setFormTaxCode(e.target.value)}
                  placeholder="e.g. 0400123456"
                  required
                  disabled={!canEdit}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] font-mono text-[14px]"
                />
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">
                  Hotline Number <span className="text-[#dc3545]">*</span>
                </label>
                <Input 
                  value={formHotline}
                  onChange={e => setFormHotline(e.target.value)}
                  placeholder="e.g. 02363841111"
                  required
                  disabled={!canEdit}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] text-[14px]"
                />
              </div>
              
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">
                  Address Location <span className="text-[#dc3545]">*</span>
                </label>
                <AddressInput 
                  value={formAddress}
                  onChange={val => setFormAddress(val)}
                  placeholder="e.g. 54 Nguyễn Lương Bằng, Đà Nẵng"
                  required
                  disabled={!canEdit}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] text-[14px]"
                />
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Description</label>
                <Textarea 
                  value={formDescription}
                  onChange={e => setFormDescription(e.target.value)}
                  placeholder="Describe organization nature, core business..."
                  rows={4}
                  disabled={!canEdit}
                  className="border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] resize-none text-[14px]"
                />
              </div>

              <div className="flex gap-2 p-3 bg-blue-50 border border-blue-100 rounded-[4px] text-[12px] text-blue-700 leading-relaxed">
                <Info className="w-4 h-4 shrink-0 mt-0.5" />
                <span>
                  These details represent this specific business unit. Tax Code and Hotline are used in business documents.
                </span>
              </div>
            </div>

            {canEdit && (
              <div className="p-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end shrink-0">
                <Button 
                  type="submit"
                  disabled={isSaving}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-5 rounded-[4px] font-[600] text-[14px] w-full"
                >
                  <Save className="w-4 h-4 mr-2" />
                  {isSaving ? "Saving..." : "Save Settings"}
                </Button>
              </div>
            )}
          </form>
        </div>

        {/* Right Side: Tab Content */}
        <div className="flex-1 flex flex-col h-full overflow-hidden">
          {activeTab === 'users' ? (
            <UsersTab orgId={orgId} />
          ) : (
            <RolesTab orgId={orgId} />
          )}
        </div>
        
      </div>
    </div>
  );
}
