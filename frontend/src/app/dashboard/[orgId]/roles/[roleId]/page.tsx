"use client";

import React, { useState, useEffect } from "react";
import { ArrowLeft, Save } from "lucide-react";
import { PermissionGuard } from "@/components/rbac/PermissionGuard";
import { PERMISSIONS, PERMISSION_GROUPS, BACKEND_ACTIONS, RESOURCE_LABELS } from "@/config/permissions";
import { APP_ROUTES } from "@/config/constants";
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
  const [activeTab, setActiveTab] = useState<string>("crm");

  // Derived modules list with virtualized product_categories module split out from products
  const derivedModules = React.useMemo(() => {
    // Clone to avoid mutating original state/props
    const list = modules.map(m => ({
      ...m,
      permissions: m.permissions ? [...m.permissions] : []
    }));

    const productsModule = list.find(m => m.code === "products");
    if (productsModule) {
      const categoryPerms = productsModule.permissions.filter(p => p.code.startsWith("product_categories:"));
      const productPerms = productsModule.permissions.filter(p => !p.code.startsWith("product_categories:"));

      // Update products module to only have products:* permissions
      productsModule.permissions = productPerms;

      // Add virtual product_categories module to modules list
      const hasCategories = list.some(m => m.code === "product_categories");
      if (!hasCategories && categoryPerms.length > 0) {
        list.push({
          id: "product_categories_virtual",
          code: "product_categories",
          name: "Product Categories",
          description: "Manage product categories",
          permissions: categoryPerms
        });
      }
    }
    return list;
  }, [modules]);

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
    derivedModules.forEach(module => {
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
      router.push(APP_ROUTES.ADMINISTRATION.ROLES(orgId));
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
            <Link href={APP_ROUTES.ADMINISTRATION.ROLES(orgId)}>
              <button className="text-[#898989] hover:text-[#242424] transition-colors">
                <ArrowLeft className="w-5 h-5" />
              </button>
            </Link>
            <h1 className="text-[24px] font-semibold text-[#242424]">
              {isNew ? "New Role" : `Edit Role: ${roleName}`}
            </h1>
          </div>
          
          {/* <div className="flex items-center gap-4">
            {!isNew && (
              <div className="hidden sm:flex bg-[#f8f8f8] rounded-[20px] p-1 text-[12px] font-semibold">
                <div className="px-3 py-1 text-[#898989]">Draft</div>
                <div className="px-3 py-1 bg-[#0066cc] text-white rounded-[16px] shadow-[0px_1px_2px_rgba(0,0,0,0.1)]">Active</div>
                <div className="px-3 py-1 text-[#898989]">Suspended</div>
              </div>
            )}
          </div> */}
        </div>

        {/* Main Content Layout */}
        <div className="flex flex-col p-6 max-w-[1000px] w-full mx-auto">
          
          {/* Left Pane: Role Form */}
          <div className="w-full">
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
                    {PERMISSION_GROUPS.map((group) => (
                      <button
                        key={group.id}
                        onClick={() => setActiveTab(group.id)}
                        className={`px-4 py-2 text-[14px] font-semibold whitespace-nowrap transition-colors border-b-2 -mb-[1px] ${
                          activeTab === group.id
                            ? "text-[#0066cc] border-[#0066cc]"
                            : "text-[#898989] border-transparent hover:text-[#242424]"
                        }`}
                      >
                        {group.name}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Tab Content: Matrix Table */}
                <div className="overflow-x-auto pb-6">
                  <table className="w-full text-left border-collapse min-w-[800px]">
                    <thead>
                      <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0]">
                        <th className="py-3 px-6 text-[13px] font-semibold text-[#242424] w-[250px]">Module / Permission</th>
                        {BACKEND_ACTIONS.map((action) => (
                          <th key={action.key} className="py-3 px-4 text-[13px] font-semibold text-[#242424] text-center w-[120px]">
                            {action.label}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {(() => {
                        if (modulesLoading) {
                          return (
                            <tr>
                              <td colSpan={6} className="py-8 text-center text-[#898989] text-[13px]">
                                Loading permissions...
                              </td>
                            </tr>
                          );
                        }
 
                        const activeGroup = PERMISSION_GROUPS.find(g => g.id === activeTab) || PERMISSION_GROUPS[0];
                        const groupModules = activeGroup.resources
                          .map(code => derivedModules.find(m => m.code === code))
                          .filter((m): m is Exclude<typeof m, undefined> => m !== undefined);
 
                        if (groupModules.length === 0) {
                          return (
                            <tr>
                              <td colSpan={6} className="py-8 text-center text-[#898989] text-[13px]">
                                Không có dữ liệu quyền hạn cho phân hệ này.
                              </td>
                            </tr>
                          );
                        }
 
                        return groupModules.map((module, idx) => (
                          <tr 
                            key={module.code} 
                            className={`border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors ${idx % 2 === 1 ? 'bg-[#fafafa]' : 'bg-white'}`}
                          >
                            <td className="py-3 px-6 text-[13px] text-[#242424] font-medium border-r border-[#e0e0e0]/50">
                              {RESOURCE_LABELS[module.code] || module.name}
                            </td>
                            {BACKEND_ACTIONS.map(action => {
                              const specificPerm = module.permissions?.find(p => p.code === `${module.code}:${action.key}`);
                              
                              return (
                                <td key={action.key} className="py-3 px-4 text-center border-r border-[#e0e0e0]/50 last:border-r-0">
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
                <Link href={APP_ROUTES.ADMINISTRATION.ROLES(orgId)}>
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


        </div>
      </div>
    </PermissionGuard>
  );
}
