"use client";

import React, { useState, useEffect } from "react";
import { ArrowLeft, Save, FileText, Calendar, Settings as SettingsIcon } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS } from "@/config/permissions";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useAllErpModules } from "@/features/organization/hooks/useAllErpModules";
import { useRoleDetail } from "@/features/organization/hooks/useRoles";
import { createRoleApi, updateRoleApi } from "@/features/organization/services/roleService";
import { toast } from "sonner";

export default function RoleFormPage() {
  const params = useParams();
  const router = useRouter();
  const orgId = params.orgId as string;
  const roleId = params.roleId as string;
  const isNew = roleId === "new";

  const { role, loading: roleLoading, error: roleError } = useRoleDetail(orgId, roleId);
  const { modules, loading: modulesLoading } = useAllErpModules(orgId);

  // Form State
  const [roleName, setRoleName] = useState("");
  const [roleCode, setRoleCode] = useState("");
  const [description, setDescription] = useState("");
  
  // Permissions State: a set of "permission_code" strings
  const [permissions, setPermissions] = useState<Set<string>>(new Set());

  // Active Tab State
  const [activeTab, setActiveTab] = useState<string>("");

  useEffect(() => {
    if (role && !isNew) {
      setRoleName(role.name || "");
      setRoleCode(role.name.toUpperCase().replace(/\s+/g, '_') || "");
      
      const permSet = new Set<string>();
      if (role.permissions) {
        role.permissions.forEach(p => permSet.add(p.code));
      }
      setPermissions(permSet);
    }
  }, [role, isNew]);

  // Set initial active tab when modules load
  useEffect(() => {
    if (modules.length > 0 && !activeTab) {
      setActiveTab(modules[0].code);
    }
  }, [modules, activeTab]);

  const togglePermission = (permCode: string) => {
    setPermissions(prev => {
      const newSet = new Set(prev);
      if (newSet.has(permCode)) {
        newSet.delete(permCode);
      } else {
        newSet.add(permCode);
      }
      return newSet;
    });
  };

  const [isSaving, setIsSaving] = useState(false);

  const handleSave = async () => {
    if (!roleName.trim()) {
      toast.error("Role Name is required");
      return;
    }

    // Map permission codes back to their UUIDs
    const permissionIds: string[] = [];
    modules.forEach(module => {
      module.permissions?.forEach(perm => {
        if (permissions.has(perm.code)) {
          permissionIds.push(perm.id);
        }
      });
    });

    try {
      setIsSaving(true);
      if (isNew) {
        await createRoleApi(orgId, {
          name: roleName,
          permissionIds
        });
        toast.success("Role created successfully");
      } else {
        await updateRoleApi(orgId, roleId, {
          name: roleName,
          permissionIds
        });
        toast.success("Role updated successfully");
      }
      router.push(`/dashboard/${orgId}/roles`);
    } catch (err: unknown) {
      console.error("Failed to save role:", err);
      toast.error((err as {response?: {data?: {message?: string}}})?.response?.data?.message || "Failed to save role");
    } finally {
      setIsSaving(false);
    }
  };

  if (roleLoading && !isNew) {
    return <div className="p-8 text-center text-[#898989]">Loading role details...</div>;
  }

  if (roleError) {
    return <div className="p-8 text-center text-[#dc3545]">Error: {roleError}</div>;
  }

  return (
    <PermissionGuard 
      permission={isNew ? PERMISSIONS.ROLES.CREATE : PERMISSIONS.ROLES.WRITE} 
      fallback={<div className="p-8 text-center text-[#dc3545]">Access Denied. You don&apos;t have permission to edit roles.</div>}
    >
      <div className="h-full bg-[#f8f8f8] flex flex-col font-['Segoe_UI',_sans-serif] overflow-auto">
        
        {/* Header Ribbon (Odoo Style) */}
        <div className="bg-white border-b border-[#e0e0e0] px-6 py-4 sticky top-0 z-20 flex justify-between items-center shrink-0">
          <div className="flex items-center gap-4">
            <Link href={`/dashboard/${orgId}/roles`}>
              <button className="text-[#898989] hover:text-[#242424] transition-colors">
                <ArrowLeft className="w-5 h-5" />
              </button>
            </Link>
            <h1 className="text-[24px] font-semibold text-[#242424]">
              {isNew ? "New Role" : `Edit Role: ${roleName}`}
            </h1>
          </div>
          
          <div className="flex items-center gap-4">
            {/* Status Ribbons */}
            {!isNew && (
              <div className="hidden sm:flex bg-[#f8f8f8] rounded-[20px] p-1 text-[12px] font-semibold">
                <div className="px-3 py-1 text-[#898989]">Draft</div>
                <div className="px-3 py-1 bg-[#0066cc] text-white rounded-[16px] shadow-[0px_1px_2px_rgba(0,0,0,0.1)]">Active</div>
                <div className="px-3 py-1 text-[#898989]">Suspended</div>
              </div>
            )}
          </div>
        </div>

        {/* Main Content Layout */}
        <div className="flex flex-col lg:flex-row p-6 gap-6 max-w-[1600px] w-full mx-auto">
          
          {/* Left Pane: Role Form */}
          <div className="flex-1 min-w-0">
            <div className="bg-white shadow-[0px_1px_3px_rgba(0,0,0,0.12)] rounded-[4px] border border-[#e0e0e0] overflow-hidden">
              
              {/* Top Section: Basic Info */}
              <div className="p-6">
                <h2 className="text-[18px] font-semibold text-[#242424] mb-4">Role Details</h2>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                  <div>
                    <label className="block text-[14px] font-semibold text-[#242424] mb-2">
                      Role Name <span className="text-[#dc3545]">*</span>
                    </label>
                    <input
                      type="text"
                      value={roleName}
                      onChange={(e) => setRoleName(e.target.value)}
                      className="w-full px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] focus:outline-none focus:border-[#0066cc]"
                      placeholder="e.g. Sales Manager"
                    />
                  </div>
                  <div>
                    <label className="block text-[14px] font-semibold text-[#242424] mb-2">
                      Role Code <span className="text-[#dc3545]">*</span>
                    </label>
                    <input
                      type="text"
                      value={roleCode}
                      onChange={(e) => setRoleCode(e.target.value)}
                      className="w-full px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] focus:outline-none focus:border-[#0066cc]"
                      placeholder="e.g. SALES_MGR"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-[14px] font-semibold text-[#242424] mb-2">
                    Description
                  </label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] focus:outline-none focus:border-[#0066cc] min-h-[80px]"
                    placeholder="Describe the permissions and scope of this role..."
                  />
                </div>
              </div>

              {/* Bottom Section: Granular Permissions (Notebooks/Tabs) */}
              <div className="mt-4 border-t border-[#e0e0e0]">
                <div className="px-6 pt-6 pb-2">
                  <h2 className="text-[18px] font-semibold text-[#242424] mb-4">Granular Permissions</h2>
                  
                  {/* Tabs Header */}
                  <div className="flex border-b border-[#e0e0e0] overflow-x-auto no-scrollbar">
                    {modulesLoading ? (
                      <div className="text-[14px] text-[#898989] py-2">Loading modules...</div>
                    ) : (
                      modules.map((module) => (
                        <button
                          key={module.code}
                          onClick={() => setActiveTab(module.code)}
                          className={`px-4 py-2 text-[14px] font-semibold whitespace-nowrap transition-colors border-b-2 -mb-[1px] ${
                            activeTab === module.code
                              ? "text-[#0066cc] border-[#0066cc]"
                              : "text-[#898989] border-transparent hover:text-[#242424]"
                          }`}
                        >
                          {module.name}
                        </button>
                      ))
                    )}
                  </div>
                </div>

                {/* Tab Content: Matrix Table */}
                <div className="overflow-x-auto pb-6">
                  <table className="w-full text-left border-collapse min-w-[800px]">
                    <thead>
                      <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                        <th className="py-3 px-6 text-[13px] font-semibold text-[#242424] w-[250px] capitalize">Resource / Feature</th>
                        {/* Dynamically render action columns based on the current active module's permissions */}
                        {(() => {
                          const activeModuleData = modules.find(m => m.code === activeTab);
                          if (!activeModuleData || !activeModuleData.permissions) return null;
                          
                          // Extract unique actions across all permissions in this module
                          const actions = Array.from(new Set(activeModuleData.permissions.map(p => {
                            const parts = p.code.split(':');
                            return parts.length > 1 ? parts[1] : 'access';
                          }))).sort();

                          return actions.map(action => (
                            <th key={action} className="py-3 px-4 text-[13px] font-semibold text-[#242424] text-center w-[120px] capitalize">
                              {action}
                            </th>
                          ));
                        })()}
                      </tr>
                    </thead>
                    <tbody>
                      {(() => {
                        const activeModuleData = modules.find(m => m.code === activeTab);
                        if (!activeModuleData || !activeModuleData.permissions || activeModuleData.permissions.length === 0) {
                          return (
                            <tr>
                              <td colSpan={10} className="py-8 text-center text-[#898989] text-[13px]">
                                No granular permissions found for this module.
                              </td>
                            </tr>
                          );
                        }

                        // Group permissions by resource (the part before ':')
                        const resourceMap = new Map<string, typeof activeModuleData.permissions>();
                        activeModuleData.permissions.forEach(p => {
                          const parts = p.code.split(':');
                          const resource = parts[0];
                          if (!resourceMap.has(resource)) {
                            resourceMap.set(resource, []);
                          }
                          resourceMap.get(resource)!.push(p);
                        });

                        // Get all unique actions for columns
                        const allActions = Array.from(new Set(activeModuleData.permissions.map(p => {
                          const parts = p.code.split(':');
                          return parts.length > 1 ? parts[1] : 'access';
                        }))).sort();

                        return Array.from(resourceMap.entries()).map(([resource, perms], idx) => (
                          <tr 
                            key={resource} 
                            className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors ${idx % 2 === 1 ? 'bg-[#fafafa]' : 'bg-white'}`}
                          >
                            <td className="py-3 px-6 text-[13px] text-[#242424] font-medium border-r border-[#e0e0e0]/50 capitalize">
                              {resource.replace(/_/g, ' ')}
                            </td>
                            {allActions.map(action => {
                              // Find if this specific permission (resource:action) exists
                              const specificPerm = perms.find(p => p.code === `${resource}:${action}`);
                              
                              return (
                                <td key={action} className="py-3 px-4 text-center border-r border-[#e0e0e0]/50 last:border-r-0">
                                  {specificPerm ? (
                                    <input 
                                      type="checkbox" 
                                      className="w-[18px] h-[18px] accent-[#0066cc] cursor-pointer rounded-[2px]"
                                      checked={permissions.has(specificPerm.code)}
                                      onChange={() => togglePermission(specificPerm.code)}
                                      title={specificPerm.description}
                                    />
                                  ) : (
                                    <span className="text-[#d0d0d0]">-</span>
                                  )}
                                </td>
                              );
                            })}
                          </tr>
                        ));
                      })()}
                    </tbody>
                  </table>
                </div>
              </div>
              
              {/* Form Footer */}
              <div className="p-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end gap-3">
                <Link href={`/dashboard/${orgId}/roles`}>
                  <button className="px-5 py-2 border border-[#d0d0d0] bg-white rounded-[4px] text-[#242424] text-[14px] font-semibold hover:bg-[#f0f0f0] transition-colors">
                    Cancel
                  </button>
                </Link>
                <button 
                  onClick={handleSave}
                  disabled={isSaving}
                  className={`px-5 py-2 rounded-[4px] text-[14px] font-semibold transition-colors flex items-center gap-2 ${isSaving ? 'bg-[#d0d0d0] text-[#898989] cursor-not-allowed' : 'bg-[#0066cc] text-white hover:bg-[#004499]'}`}
                >
                  <Save className="w-4 h-4" />
                  {isSaving ? "Saving..." : "Save Role"}
                </button>
              </div>
            </div>
          </div>

          {/* Right Pane: Odoo Chatter / Log Notes */}
          <div className="w-full lg:w-[400px] shrink-0 flex flex-col">
            <div className="bg-white shadow-[0px_1px_3px_rgba(0,0,0,0.12)] rounded-[4px] border border-[#e0e0e0] flex-1">
              
              {/* Chatter Header / Action Buttons */}
              <div className="flex border-b border-[#e0e0e0]">
                <button className="flex-1 py-3 text-[14px] font-semibold flex items-center justify-center gap-2 hover:bg-[#f8f8f8] transition-colors text-[#242424] border-r border-[#e0e0e0]">
                  <FileText className="w-4 h-4 text-[#898989]" />
                  Log Note
                </button>
                <button className="flex-1 py-3 text-[14px] font-semibold flex items-center justify-center gap-2 hover:bg-[#f8f8f8] transition-colors text-[#242424]">
                  <Calendar className="w-4 h-4 text-[#898989]" />
                  Schedule Activity
                </button>
              </div>

              {/* Log Timeline */}
              <div className="p-4 relative">
                {/* Vertical Timeline Line */}
                <div className="absolute left-[36px] top-4 bottom-4 w-px bg-[#e0e0e0] z-0"></div>

                {isNew ? (
                  <div className="relative z-10 flex gap-4">
                    <div className="w-10 h-10 rounded-full border border-[#d0d0d0] shrink-0 bg-white flex items-center justify-center">
                      <SettingsIcon className="w-5 h-5 text-[#898989]" />
                    </div>
                    <div className="flex-1 mt-1">
                      <div className="flex justify-between items-baseline mb-1">
                        <span className="font-semibold text-[14px] text-[#898989]">System</span>
                        <span className="text-[12px] text-[#898989]">Just now</span>
                      </div>
                      <p className="text-[13px] text-[#898989] italic">
                        Creating new role. You can assign granular permissions and save.
                      </p>
                    </div>
                  </div>
                ) : (
                  <>
                    {/* Entry 1: Current Status */}
                    <div className="relative z-10 flex gap-4 mb-6">
                      <div className="w-10 h-10 rounded-full border border-[#d0d0d0] shrink-0 bg-white overflow-hidden">
                        {/* eslint-disable-next-line @next/next/no-img-element */}
                        <img src={`https://api.dicebear.com/7.x/initials/svg?seed=${role?.name || 'Role'}`} alt="Role Avatar" className="w-full h-full object-cover" />
                      </div>
                      <div className="flex-1 mt-1">
                        <div className="flex justify-between items-baseline mb-1">
                          <span className="font-semibold text-[14px] text-[#242424]">{role?.name || 'Role'} Status</span>
                          <span className="text-[12px] text-[#898989]">Current</span>
                        </div>
                        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] p-3 mt-2 text-[13px] text-[#242424]">
                          This role is actively managed under organization <strong>{role?.organization?.name || 'your organization'}</strong>.
                        </div>
                      </div>
                    </div>

                    {/* Entry 2: Permission Count */}
                    <div className="relative z-10 flex gap-4">
                      <div className="w-10 h-10 rounded-full border border-[#d0d0d0] shrink-0 bg-white flex items-center justify-center">
                        <SettingsIcon className="w-5 h-5 text-[#898989]" />
                      </div>
                      <div className="flex-1 mt-1">
                        <div className="flex justify-between items-baseline mb-1">
                          <span className="font-semibold text-[14px] text-[#898989]">System Audit</span>
                          <span className="text-[12px] text-[#898989]">Auto-generated</span>
                        </div>
                        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] p-3 mt-2">
                          <p className="text-[13px] text-[#242424] mb-3">
                            Currently holds <strong>{permissions.size}</strong> granular permissions.
                          </p>
                          <div className="space-y-1 font-mono text-[12px]">
                            <div className="bg-[#f0f4ff] text-[#0066cc] px-2 py-1 rounded">
                              + Synced with module definitions.
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </>
                )}

              </div>
            </div>
          </div>

        </div>
      </div>
    </PermissionGuard>
  );
}
