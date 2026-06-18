// src/app/(dashboard)/administration/page.tsx
// Administration Page - Main page for managing organizations, roles, and users
// Follows DESIGN.md: Layout, spacing, typography

'use client';

import React, { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { OrganizationList } from '@/features/administration/components/OrganizationList';
import { OrganizationForm } from '@/features/administration/components/OrganizationForm';
import { DefineRole } from '@/features/administration/components/DefineRole';
import { UserList } from '@/features/administration/components/UserList';
import { UserForm } from '@/features/administration/components/UserForm';
import { useOrganizations } from '@/features/administration/hooks/useOrganizations';
import { useRoles } from '@/features/administration/hooks/useRoles';
import { useUsers } from '@/features/administration/hooks/useUsers';
import type { Organization, Role, User, OrganizationFormData, RoleFormData, UserFormData } from '@/features/administration/types';

export default function AdministrationPage() {
  return (
    <PermissionGuard
      permission="administration:read"
      fallback={
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h1>
            <p className="text-gray-600">You don&apos;t have permission to access the administration panel.</p>
          </div>
        </div>
      }
    >
      <AdministrationContent />
    </PermissionGuard>
  );
}

function AdministrationContent() {
  const { organizations, loading: orgLoading, createOrganization, updateOrganization, deleteOrganization } = useOrganizations();
  const { roles, loading: roleLoading, createRole, updateRole, deleteRole } = useRoles();
  const { users, loading: userLoading, createUser, updateUser, deleteUser, toggleUserStatus } = useUsers();

  // Modal states
  const [orgFormOpen, setOrgFormOpen] = useState(false);
  const [editingOrg, setEditingOrg] = useState<Organization | null>(null);
  const [roleFormOpen, setRoleFormOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<Role | null>(null);
  const [userFormOpen, setUserFormOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);

  // Organization handlers
  const handleAddOrg = () => {
    setEditingOrg(null);
    setOrgFormOpen(true);
  };

  const handleEditOrg = (org: Organization) => {
    setEditingOrg(org);
    setOrgFormOpen(true);
  };

  const handleDeleteOrg = async (org: Organization) => {
    if (confirm(`Are you sure you want to delete organization "${org.name}"?`)) {
      try {
        await deleteOrganization(org.id);
      } catch {
        // Error handled in hook
      }
    }
  };

  const handleSubmitOrg = async (data: OrganizationFormData) => {
    try {
      if (editingOrg) {
        await updateOrganization(editingOrg.id, data);
      } else {
        await createOrganization(data);
      }
      setOrgFormOpen(false);
    } catch {
      // Error handled in hook
    }
  };

  // Role handlers
  const handleAddRole = () => {
    setEditingRole(null);
    setRoleFormOpen(true);
  };

  const handleEditRole = (role: Role) => {
    setEditingRole(role);
    setRoleFormOpen(true);
  };

  const handleDeleteRole = async (role: Role) => {
    if (confirm(`Are you sure you want to delete role "${role.name}"?`)) {
      try {
        await deleteRole(role.id);
      } catch {
        // Error handled in hook
      }
    }
  };

  const handleSubmitRole = async (data: RoleFormData) => {
    try {
      if (editingRole) {
        await updateRole(editingRole.id, data);
      } else {
        // Use first organization as default for system admin context
        const defaultOrgId = organizations[0]?.id || 'default-org';
        await createRole({ ...data, organizationId: defaultOrgId });
      }
      setRoleFormOpen(false);
    } catch {
      // Error handled in hook
    }
  };

  const handleRoleCreated = () => {
    // Add the new role to the roles list
    // This would normally be handled by the hook refetching data
    // For now, we'll just close the modal
  };

  // User handlers
  const handleAddUser = () => {
    setEditingUser(null);
    setUserFormOpen(true);
  };

  const handleEditUser = (user: User) => {
    setEditingUser(user);
    setUserFormOpen(true);
  };

  const handleDeleteUser = async (user: User) => {
    if (confirm(`Are you sure you want to delete user "${user.firstName} ${user.lastName}"?`)) {
      try {
        await deleteUser(user.id);
      } catch {
        // Error handled in hook
      }
    }
  };

  const handleToggleUserStatus = async (user: User) => {
    try {
      await toggleUserStatus(user.id);
    } catch {
      // Error handled in hook
    }
  };

  const handleSubmitUser = async (data: UserFormData) => {
    try {
      if (editingUser) {
        await updateUser(editingUser.id, data);
      } else {
        // Use first organization as default for system admin context
        const defaultOrgId = organizations[0]?.id || 'default-org';
        await createUser({ ...data, organizationId: defaultOrgId });
      }
      setUserFormOpen(false);
    } catch {
      // Error handled in hook
    }
  };

  const handleQuickCreateRole = () => {
    setRoleFormOpen(true);
    setEditingRole(null);
  };

  return (
    <div className="container mx-auto px-6 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-charcoal mb-2">System Administration</h1>
        <p className="text-mid-gray">
          Manage organizations, roles, and users in the ERP system
        </p>
      </div>

      <Tabs defaultValue="organizations" className="space-y-6">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="organizations">Organizations</TabsTrigger>
          <TabsTrigger value="roles">Roles</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
        </TabsList>

        <TabsContent value="organizations" className="space-y-6">
          <PermissionGuard permission="admin:read" fallback={<div>You do not have permission to access this page</div>}>
            <OrganizationList
              organizations={organizations}
              onAdd={handleAddOrg}
              onEdit={handleEditOrg}
              onDelete={handleDeleteOrg}
            />
          </PermissionGuard>
        </TabsContent>

        <TabsContent value="roles" className="space-y-6">
          <PermissionGuard permission="admin:read" fallback={<div>You do not have permission to access this page</div>}>
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-semibold text-charcoal">Role Management</h2>
              <button
                onClick={handleAddRole}
                className="bg-primary-blue hover:bg-dark-blue text-white px-4 py-2 rounded-md font-medium"
              >
                Define New Role
              </button>
            </div>

            <div className="grid gap-4">
              {roles.map((role) => (
                <div key={role.id} className="bg-white p-4 rounded-md shadow-standard border">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-semibold text-charcoal">{role.name}</h3>
                      <p className="text-sm text-mid-gray mt-1">
                        Permissions: {role.permissions.join(', ')}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEditRole(role)}
                        className="text-primary-blue hover:bg-light-blue px-3 py-1 rounded text-sm"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteRole(role)}
                        className="text-error-red hover:bg-light-red px-3 py-1 rounded text-sm"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </PermissionGuard>
        </TabsContent>

        <TabsContent value="users" className="space-y-6">
          <PermissionGuard permission="admin:read" fallback={<div>You do not have permission to access this page</div>}>
            <UserList
              users={users}
              roles={roles}
              onAdd={handleAddUser}
              onEdit={handleEditUser}
              onDelete={handleDeleteUser}
              onToggleStatus={handleToggleUserStatus}
            />
          </PermissionGuard>
        </TabsContent>
      </Tabs>

      {/* Modals */}
      <OrganizationForm
        isOpen={orgFormOpen}
        onClose={() => setOrgFormOpen(false)}
        onSubmit={handleSubmitOrg}
        organization={editingOrg}
        loading={orgLoading}
      />

      <DefineRole
        isOpen={roleFormOpen}
        onClose={() => setRoleFormOpen(false)}
        onSubmit={handleSubmitRole}
        role={editingRole}
        loading={roleLoading}
      />

      <UserForm
        isOpen={userFormOpen}
        onClose={() => setUserFormOpen(false)}
        onSubmit={handleSubmitUser}
        //onQuickCreateRole={handleQuickCreateRole}
        onRoleCreated={handleRoleCreated}
        user={editingUser}
        roles={roles}
        loading={userLoading}
      />
    </div>
  );
}
