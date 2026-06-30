// src/features/administration/mock-data.ts
// Mock data for Administration features
// When backend is ready, replace with real API calls

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

// Mock Organizations
export const mockOrganizations: Organization[] = [
  {
    id: 'org-1',
    name: 'ABC Company Ltd',
    code: 'ABC',
    logo: '/logos/abc-logo.png',
    contactEmail: 'contact@abc.com',
    contactPhone: '+84 123 456 789',
    address: '123 ABC Street, District 1, HCMC',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'org-2',
    name: 'XYZ Joint Stock Company',
    code: 'XYZ',
    logo: '/logos/xyz-logo.png',
    contactEmail: 'info@xyz.com',
    contactPhone: '+84 987 654 321',
    address: '456 XYZ Street, District 2, HCMC',
    createdAt: '2024-02-01T00:00:00Z',
    updatedAt: '2024-02-01T00:00:00Z',
  },
  {
    id: 'org-3',
    name: 'DEF Private Enterprise',
    code: 'DEF',
    contactEmail: 'admin@def.com',
    contactPhone: '+84 555 666 777',
    address: '789 DEF Street, District 3, HCMC',
    createdAt: '2024-03-01T00:00:00Z',
    updatedAt: '2024-03-01T00:00:00Z',
  },
];

// Mock Roles
export const mockRoles: Role[] = [
  {
    id: 'role-1',
    name: 'Warehouse Manager',
    permissions: ['inventory:create', 'inventory:read', 'inventory:update', 'inventory:delete', 'inventory:export', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'role-2',
    name: 'Sales Associate',
    permissions: ['sales:create', 'sales:read', 'sales:update', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  },
  {
    id: 'role-3',
    name: 'Accountant',
    permissions: ['finance:read', 'finance:update', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-03T00:00:00Z',
  },
  {
    id: 'role-4',
    name: 'Human Resources',
    permissions: ['hr:create', 'hr:read', 'hr:update', 'hr:delete', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-04T00:00:00Z',
    updatedAt: '2024-01-04T00:00:00Z',
  },
  {
    id: 'role-5',
    name: 'Blockchain Auditor',
    permissions: ['blockchain_audit:read', 'blockchain_audit:export', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-05T00:00:00Z',
    updatedAt: '2024-01-05T00:00:00Z',
  },
  {
    id: 'role-6',
    name: 'System Admin',
    permissions: [
      'sales:create', 'sales:read', 'sales:update', 'sales:delete', 'sales:export',
      'inventory:create', 'inventory:read', 'inventory:update', 'inventory:delete', 'inventory:export',
      'hr:create', 'hr:read', 'hr:update', 'hr:delete', 'hr:export',
      'finance:create', 'finance:read', 'finance:update', 'finance:delete', 'finance:export',
      'dashboard:read',
      'administration:read', 'administration:create', 'administration:update', 'administration:delete',
      'organizations:manage',
      'roles:manage',
      'users:manage'
    ],
    organizationId: 'org-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
];

// Mock Users
export const mockUsers: User[] = [
  {
    id: 'user-1',
    email: 'john.doe@abc.com',
    firstName: 'John',
    lastName: 'Doe',
    roleId: 'role-2',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-10T00:00:00Z',
    updatedAt: '2024-01-10T00:00:00Z',
  },
  {
    id: 'user-2',
    email: 'jane.smith@abc.com',
    firstName: 'Jane',
    lastName: 'Smith',
    roleId: 'role-1',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-11T00:00:00Z',
    updatedAt: '2024-01-11T00:00:00Z',
  },
  {
    id: 'user-3',
    email: 'robert.johnson@abc.com',
    firstName: 'Robert',
    lastName: 'Johnson',
    roleId: 'role-4',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'user-4',
    email: 'emily.davis@abc.com',
    firstName: 'Emily',
    lastName: 'Davis',
    roleId: 'role-3',
    organizationId: 'org-1',
    isActive: false,
    createdAt: '2024-01-12T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z',
  },
  {
    id: 'user-5',
    email: 'william.brown@abc.com',
    firstName: 'William',
    lastName: 'Brown',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-13T00:00:00Z',
    updatedAt: '2024-01-13T00:00:00Z',
  },
];
