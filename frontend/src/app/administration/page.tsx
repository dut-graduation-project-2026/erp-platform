// src/app/administration/page.tsx
// Administration Page - Modern ERP-style layout with sidebar and header
// Follows DESIGN.md: Professional ERP interface with navigation and content areas

'use client';

import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { OrganizationList } from '@/features/administration/components/OrganizationList';
import { OrganizationForm } from '@/features/administration/components/OrganizationForm';
import { DefineRole } from '@/features/administration/components/DefineRole';
import { UserList } from '@/features/administration/components/UserList';
import { UserForm } from '@/features/administration/components/UserForm';
import { useOrganizations } from '@/features/administration/hooks/useOrganizations';
import { useRoles } from '@/features/administration/hooks/useRoles';
import { useUsers } from '@/features/administration/hooks/useUsers';
import {
  Building2,
  Users,
  Shield,
  Plus,
  Settings,
  BarChart3
} from 'lucide-react';
import type { Organization, Role, User, OrganizationFormData, RoleFormData, UserFormData } from '@/features/administration/types';

export default function AdministrationPage() {
  return (
    <PermissionGuard
      permission="administration:read"
      fallback={
        <div className="flex items-center justify-center min-h-[400px]">
          <Card className="w-full max-w-md">
            <CardHeader className="text-center">
              <Shield className="h-12 w-12 text-red-500 mx-auto mb-4" />
              <CardTitle className="text-red-600">Access Denied</CardTitle>
              <CardDescription>
                You don't have permission to access the administration panel.
              </CardDescription>
            </CardHeader>
          </Card>
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
    if (confirm(`Are you sure you want to delete organization "${org.name}"? This action cannot be undone.`)) {
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

  const handleRoleCreated = (role: Role) => {
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
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Administration</h1>
          <p className="text-gray-600 mt-1">
            Manage organizations, roles, and users across the platform
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Button variant="outline" size="sm">
            <BarChart3 className="h-4 w-4 mr-2" />
            Analytics
          </Button>
          <Button variant="outline" size="sm">
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </Button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Organizations</CardTitle>
            <Building2 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{organizations.length}</div>
            <p className="text-xs text-muted-foreground">
              +2 from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Active Users</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{users.filter(u => u.isActive).length}</div>
            <p className="text-xs text-muted-foreground">
              +12 from last month
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Roles Defined</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{roles.length}</div>
            <p className="text-xs text-muted-foreground">
              +3 from last month
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Main Content Tabs */}
      <div className="space-y-6">
        {/* Organizations Section */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center">
                  <Building2 className="h-5 w-5 mr-2" />
                  Organizations
                </CardTitle>
                <CardDescription>
                  Manage tenant organizations and their configurations
                </CardDescription>
              </div>
              <Button onClick={handleAddOrg}>
                <Plus className="h-4 w-4 mr-2" />
                Add Organization
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <OrganizationList
              organizations={organizations}
              onAdd={handleAddOrg}
              onEdit={handleEditOrg}
              onDelete={handleDeleteOrg}
            />
          </CardContent>
        </Card>

        {/* Roles Section */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center">
                  <Shield className="h-5 w-5 mr-2" />
                  Roles & Permissions
                </CardTitle>
                <CardDescription>
                  Define roles and assign granular permissions
                </CardDescription>
              </div>
              <Button onClick={handleAddRole}>
                <Plus className="h-4 w-4 mr-2" />
                Define Role
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {roles.map((role) => (
                <div key={role.id} className="flex items-center justify-between p-4 border rounded-lg">
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900">{role.name}</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Permissions: {role.permissions.join(', ')}
                    </p>
                    <div className="flex flex-wrap gap-1 mt-2">
                      {role.permissions.slice(0, 3).map((perm) => (
                        <Badge key={perm} variant="secondary" className="text-xs">
                          {perm}
                        </Badge>
                      ))}
                      {role.permissions.length > 3 && (
                        <Badge variant="outline" className="text-xs">
                          +{role.permissions.length - 3} more
                        </Badge>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleEditRole(role)}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDeleteRole(role)}
                      className="text-red-600 hover:text-red-700"
                    >
                      Delete
                    </Button>
                  </div>
                </div>
              ))}
              {roles.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                  <Shield className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                  <p>No roles defined yet</p>
                  <Button onClick={handleAddRole} className="mt-4">
                    <Plus className="h-4 w-4 mr-2" />
                    Define First Role
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Users Section */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center">
                  <Users className="h-5 w-5 mr-2" />
                  User Management
                </CardTitle>
                <CardDescription>
                  Manage user accounts and role assignments
                </CardDescription>
              </div>
              <Button onClick={handleAddUser}>
                <Plus className="h-4 w-4 mr-2" />
                Add User
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            <UserList
              users={users}
              roles={roles}
              onAdd={handleAddUser}
              onEdit={handleEditUser}
              onDelete={handleDeleteUser}
              onToggleStatus={handleToggleUserStatus}
            />
          </CardContent>
        </Card>
      </div>

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
        onQuickCreateRole={handleQuickCreateRole}
        onRoleCreated={handleRoleCreated}
        user={editingUser}
        roles={roles}
        loading={userLoading}
      />
    </div>
  );
}
