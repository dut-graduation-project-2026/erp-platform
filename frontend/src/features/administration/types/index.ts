// src/features/administration/types/index.ts
// Type definitions for Administration features

export interface Organization {
  id: string;
  name: string;
  code: string;
  logo?: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Role {
  id: string;
  name: string;
  permissions: string[]; // Array of 'resource:action' strings
  organizationId: string;
  createdAt: string;
  updatedAt: string;
}

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  roleId?: string;
  organizationId: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PermissionMatrix {
  resource: string;
  actions: Record<string, boolean>; // action -> checked state
}

export interface RoleFormData {
  name: string;
  permissions: string[];
}

export interface UserFormData {
  email: string;
  firstName: string;
  lastName: string;
  roleId?: string;
}

export interface OrganizationFormData {
  name: string;
  code: string;
  logo?: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
}
