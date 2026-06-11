"use client";

import React, { useState } from "react";
import { Search, Plus, Filter, MoreHorizontal, Building2, MapPin, Phone, Edit, Info, X, Save, RefreshCw } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import { useOrganizations } from "@/features/organization/hooks/useOrganizations";
import { usePermissions } from "@/hooks/use-permissions";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";

export default function OrganizationsPage() {
  const { organizations, loading, refetch, createOrganization, updateOrganization } = useOrganizations();
  const { hasPermission } = usePermissions();
  const [searchTerm, setSearchTerm] = useState("");
  
  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingOrg, setEditingOrg] = useState<any | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Form Fields
  const [formName, setFormName] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formAddress, setFormAddress] = useState("");
  const [formHotline, setFormHotline] = useState("");
  const [formTaxCode, setFormTaxCode] = useState("");

  const handleOpenCreateModal = () => {
    setEditingOrg(null);
    setFormName("");
    setFormDescription("");
    setFormAddress("");
    setFormHotline("");
    setFormTaxCode("");
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (org: any) => {
    setEditingOrg(org);
    setFormName(org.name || "");
    setFormDescription(org.description || "");
    setFormAddress(org.address || "");
    setFormHotline(org.hotline || "");
    setFormTaxCode(org.taxCode || "");
    setIsModalOpen(true);
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
      let success = false;
      if (editingOrg) {
        success = await updateOrganization(editingOrg.id, payload);
      } else {
        success = await createOrganization(payload);
      }

      if (success) {
        setIsModalOpen(false);
        refetch();
      }
    } catch (err) {
      console.error(err);
      toast.error("Failed to save organization. Please check details.");
    } finally {
      setIsSaving(false);
    }
  };

  const filteredOrgs = organizations.filter((org) => {
    const q = searchTerm.toLowerCase();
    return (
      org.name.toLowerCase().includes(q) ||
      org.address?.toLowerCase().includes(q) ||
      org.taxCode?.toLowerCase().includes(q) ||
      org.description?.toLowerCase().includes(q)
    );
  });

  return (
    <PermissionGuard 
      permission={PERMISSIONS.ORGANIZATIONS.READ} 
      fallback={
        <div className="p-8 text-center text-[#dc3545] font-['Segoe_UI']">
          Access Denied. You do not have permission to view organizations.
        </div>
      }
    >
      <div className="h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif]">
        {/* Top Control Bar */}
        <div className="border-b border-[#e0e0e0] px-8 py-5 flex flex-col gap-4 bg-white shrink-0 shadow-[0px_1px_2px_rgba(0,0,0,0.05)]">
          <div className="flex items-center text-[12px] text-[#898989] font-[600] tracking-wide uppercase">
            <span className="hover:text-[#0066cc] cursor-pointer">ERP Platform</span>
            <span className="mx-2">/</span>
            <span className="text-[#0066cc] font-[600]">Organizations</span>
          </div>

          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-[32px] font-bold text-[#242424] leading-[1.2] tracking-tight">Organizations Directory</h1>
              <p className="text-[13px] text-[#898989] mt-1">Manage and organize your multi-company business units and branches</p>
            </div>
            
            <div className="flex items-center gap-3">
              <div className="relative">
                <Search className="w-4 h-4 text-[#898989] absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search by name, address, tax code..."
                  className="pl-10 pr-4 py-2 w-[280px] bg-[#f8f8f8] border border-[#d0d0d0] rounded-[4px] text-[13px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] focus:bg-white transition-all shadow-[0px_1px_2px_rgba(0,0,0,0.05)]"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              <Button
                variant="outline"
                onClick={refetch}
                className="h-10 w-10 p-0 border-[#d0d0d0] text-[#898989] hover:text-[#242424] bg-white rounded-[4px]"
              >
                <RefreshCw className={cn("w-4 h-4", loading && "animate-spin")} />
              </Button>
            </div>
          </div>

          <div className="flex items-center justify-between mt-1">
            <div className="flex items-center gap-2">
              {hasPermission(PERMISSIONS.ORGANIZATIONS.CREATE) && (
                <Button 
                  onClick={handleOpenCreateModal}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white px-4 h-10 rounded-[4px] text-[14px] font-[600] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] flex items-center gap-1.5 transition-colors"
                >
                  <Plus className="w-4 h-4" />
                  New Organization
                </Button>
              )}
            </div>
          </div>
        </div>

        {/* Main Content Area - Kanban View */}
        <div className="flex-1 overflow-auto p-8">
          {loading && organizations.length === 0 ? (
            <div className="flex flex-col justify-center items-center h-64 text-[#898989]">
              <RefreshCw className="w-8 h-8 animate-spin text-[#0066cc] mb-3" />
              <span className="text-[14px]">Fetching organizations list...</span>
            </div>
          ) : filteredOrgs.length === 0 ? (
            <div className="bg-white border border-dashed border-[#e0e0e0] rounded-[4px] p-12 text-center max-w-[600px] mx-auto mt-12 shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
              <Building2 className="w-12 h-12 text-[#898989] mx-auto mb-4" strokeWidth={1.2} />
              <h3 className="text-[16px] font-bold text-[#242424] mb-1">No Organizations Found</h3>
              <p className="text-[13px] text-[#898989] mb-5">Try adjusting your search criteria or add your first business unit.</p>
              {hasPermission(PERMISSIONS.ORGANIZATIONS.CREATE) && (
                <Button onClick={handleOpenCreateModal} className="bg-[#0066cc] text-white rounded-[4px] hover:bg-[#004499]">
                  <Plus className="w-4 h-4 mr-2" /> New Organization
                </Button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-[1400px] mx-auto">
              {filteredOrgs.map((org) => (
                <div 
                  key={org.id} 
                  className="bg-white rounded-[4px] border border-[#e0e0e0] p-6 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] hover:border-[#0066cc] transition-all flex flex-col justify-between group cursor-pointer"
                >
                  <div>
                    <div className="flex justify-between items-start mb-4">
                      <div className="w-12 h-12 rounded-[4px] bg-[#f0f4ff] border border-[#dbeafe] flex items-center justify-center text-[#0066cc]">
                        <Building2 className="w-6 h-6" strokeWidth={1.5} />
                      </div>
                      
                      {hasPermission(PERMISSIONS.ORGANIZATIONS.WRITE) && (
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleOpenEditModal(org);
                          }}
                          className="h-8 w-8 text-[#898989] hover:text-[#0066cc] hover:bg-[#f0f4ff] rounded-[4px] opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                          <Edit className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                    
                    <h3 className="text-[16px] font-bold text-[#242424] mb-2 line-clamp-1 group-hover:text-[#0066cc] transition-colors font-['Segoe_UI']" title={org.name}>
                      {org.name}
                    </h3>

                    <p className="text-[13px] text-[#898989] line-clamp-2 h-9 mb-4 italic">
                      {org.description || "No description provided."}
                    </p>

                    <div className="space-y-2.5 border-t border-[#f5f5f5] pt-4">
                      <div className="flex items-center gap-2 text-[13px] text-[#242424]">
                        <MapPin className="w-4 h-4 text-[#898989] shrink-0" />
                        <span className="line-clamp-1" title={org.address}>{org.address}</span>
                      </div>
                      <div className="flex items-center gap-2 text-[13px] text-[#242424]">
                        <Phone className="w-4 h-4 text-[#898989] shrink-0" />
                        <span>{org.hotline || "N/A"}</span>
                      </div>
                    </div>
                  </div>

                  <div className="mt-5 pt-4 border-t border-[#f5f5f5] flex justify-between items-center text-[12px]">
                    <span className="text-[#898989] bg-[#f8f8f8] px-2 py-0.5 border border-[#e0e0e0] rounded-[2px] font-mono font-medium">
                      TAX: {org.taxCode}
                    </span>
                    <span className="font-semibold text-[#0066cc] bg-[#f0f4ff] px-2.5 py-1 rounded-[4px] border border-[#dbeafe]">
                      Business Unit
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Create / Edit Modal */}
        {isModalOpen && (
          <div className="fixed inset-0 bg-[#000000]/50 backdrop-blur-[2px] z-50 flex items-center justify-center animate-in fade-in duration-200">
            <form 
              onSubmit={handleSave}
              className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[560px] flex flex-col max-h-[90vh] border border-[#e0e0e0]"
            >
              <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8] shrink-0 rounded-t-[8px]">
                <div className="flex items-center gap-2">
                  <Building2 className="w-5 h-5 text-[#0066cc]" />
                  <h2 className="text-[18px] font-bold text-[#242424]">
                    {editingOrg ? "Edit Organization Settings" : "Register New Organization"}
                  </h2>
                </div>
                <Button 
                  type="button" 
                  variant="ghost" 
                  size="icon" 
                  onClick={() => setIsModalOpen(false)} 
                  className="h-8 w-8 text-[#898989] hover:text-[#242424] rounded-[4px]"
                >
                  <X className="w-5 h-5" />
                </Button>
              </div>

              <div className="p-6 overflow-y-auto space-y-4">
                <div>
                  <label className="block text-[14px] font-[600] text-[#242424] mb-1.5">
                    Organization Name <span className="text-[#dc3545]">*</span>
                  </label>
                  <Input 
                    value={formName}
                    onChange={e => setFormName(e.target.value)}
                    placeholder="e.g. Công ty Cổ phần Công nghệ DUT"
                    required
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>

                <div>
                  <label className="block text-[14px] font-[600] text-[#242424] mb-1.5">
                    Tax Code <span className="text-[#dc3545]">*</span>
                  </label>
                  <Input 
                    value={formTaxCode}
                    onChange={e => setFormTaxCode(e.target.value)}
                    placeholder="e.g. 0400123456"
                    required
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] font-mono"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1.5">
                      Hotline Number <span className="text-[#dc3545]">*</span>
                    </label>
                    <Input 
                      value={formHotline}
                      onChange={e => setFormHotline(e.target.value)}
                      placeholder="e.g. 02363841111"
                      required
                      className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1.5">
                      Address Location <span className="text-[#dc3545]">*</span>
                    </label>
                    <Input 
                      value={formAddress}
                      onChange={e => setFormAddress(e.target.value)}
                      placeholder="e.g. Đà Nẵng, Việt Nam"
                      required
                      className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[14px] font-[600] text-[#242424] mb-1.5">Description</label>
                  <Textarea 
                    value={formDescription}
                    onChange={e => setFormDescription(e.target.value)}
                    placeholder="Describe organization nature, core business, or target markets..."
                    rows={3}
                    className="border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] resize-none"
                  />
                </div>

                <div className="flex gap-2 p-3 bg-blue-50 border border-blue-100 rounded-[4px] text-[12px] text-blue-700 leading-relaxed shrink-0">
                  <Info className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>
                    Organizations represent independent legal entities or business units. Tax Code and Hotline will be visible on purchase orders, invoices, and sales transactions.
                  </span>
                </div>
              </div>

              <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2 shrink-0 rounded-b-[8px]">
                <Button 
                  type="button"
                  variant="outline" 
                  onClick={() => setIsModalOpen(false)}
                  className="bg-white border-[#d0d0d0] text-[#242424] h-10 px-4 rounded-[4px] font-[600] text-[14px]"
                >
                  Cancel
                </Button>
                <Button 
                  type="submit"
                  disabled={isSaving}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600] text-[14px]"
                >
                  <Save className="w-4 h-4 mr-1.5" />
                  {isSaving ? "Saving..." : "Save Settings"}
                </Button>
              </div>
            </form>
          </div>
        )}
      </div>
    </PermissionGuard>
  );
}
