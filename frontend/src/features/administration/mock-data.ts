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
    name: 'Công ty TNHH ABC',
    code: 'ABC',
    logo: '/logos/abc-logo.png',
    contactEmail: 'contact@abc.com',
    contactPhone: '+84 123 456 789',
    address: '123 Đường ABC, Quận 1, TP.HCM',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'org-2',
    name: 'Công ty Cổ phần XYZ',
    code: 'XYZ',
    logo: '/logos/xyz-logo.png',
    contactEmail: 'info@xyz.com',
    contactPhone: '+84 987 654 321',
    address: '456 Đường XYZ, Quận 2, TP.HCM',
    createdAt: '2024-02-01T00:00:00Z',
    updatedAt: '2024-02-01T00:00:00Z',
  },
  {
    id: 'org-3',
    name: 'Doanh nghiệp Tư nhân DEF',
    code: 'DEF',
    contactEmail: 'admin@def.com',
    contactPhone: '+84 555 666 777',
    address: '789 Đường DEF, Quận 3, TP.HCM',
    createdAt: '2024-03-01T00:00:00Z',
    updatedAt: '2024-03-01T00:00:00Z',
  },
];

// Mock Roles
export const mockRoles: Role[] = [
  {
    id: 'role-1',
    name: 'Quản lý kho',
    permissions: ['inventory:create', 'inventory:read', 'inventory:update', 'inventory:delete', 'inventory:export', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'role-2',
    name: 'Nhân viên bán hàng',
    permissions: ['sales:create', 'sales:read', 'sales:update', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z',
  },
  {
    id: 'role-3',
    name: 'Kế toán',
    permissions: ['finance:read', 'finance:update', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-03T00:00:00Z',
  },
  {
    id: 'role-4',
    name: 'Nhân sự',
    permissions: ['hr:create', 'hr:read', 'hr:update', 'hr:delete', 'dashboard:read'],
    organizationId: 'org-1',
    createdAt: '2024-01-04T00:00:00Z',
    updatedAt: '2024-01-04T00:00:00Z',
  },
  {
    id: 'role-5',
    name: 'Kiểm toán Blockchain',
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
    email: 'nguyenvana@abc.com',
    firstName: 'Nguyễn',
    lastName: 'Văn A',
    roleId: 'role-2',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-10T00:00:00Z',
    updatedAt: '2024-01-10T00:00:00Z',
  },
  {
    id: 'user-2',
    email: 'tranthib@abc.com',
    firstName: 'Trần',
    lastName: 'Thị B',
    roleId: 'role-1',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-11T00:00:00Z',
    updatedAt: '2024-01-11T00:00:00Z',
  },
  {
    id: 'user-3',
    email: 'levanc@abc.com',
    firstName: 'Lê',
    lastName: 'Văn C',
    roleId: 'role-4',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'user-4',
    email: 'phamthid@abc.com',
    firstName: 'Phạm',
    lastName: 'Thị D',
    roleId: 'role-3',
    organizationId: 'org-1',
    isActive: false,
    createdAt: '2024-01-12T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z',
  },
  {
    id: 'user-5',
    email: 'hoangvane@abc.com',
    firstName: 'Hoàng',
    lastName: 'Văn E',
    organizationId: 'org-1',
    isActive: true,
    createdAt: '2024-01-13T00:00:00Z',
    updatedAt: '2024-01-13T00:00:00Z',
  },
];
