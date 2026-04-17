// src/services/mockOrganizations.ts
// Mock organizations for testing frontend before backend is ready

import type { UserOrganization } from '@/types/organization';

export const mockUserOrganizations: UserOrganization[] = [
  {
    id: 'org1',
    name: 'ERP Main Company',
    description: 'Main organization for ERP system',
    role: 'admin',
    permissions: ['organization:read', 'organization:write', 'user:read', 'user:write','administration:read'],
    logo: '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 'org2',
    name: 'Branch Office',
    description: 'Branch office organization',
    role: 'user',
    permissions: ['organization:read', 'user:read', 'user:write','administration:read'],
    logo: '',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  },
];
