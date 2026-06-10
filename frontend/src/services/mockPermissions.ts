// src/services/mockPermissions.ts
// Mock Permission Service for RBAC - Migration Friendly
// When backend is ready, replace this with real API calls

export interface Permission {
  id: string;
  name: string; // e.g., 'sales', 'inventory'
  resource: string; // Same as name for simplicity
  action: string; // e.g., 'create', 'read', 'update', 'delete', 'export'
}

// Mock permissions data - represents resource:action combinations
const mockPermissions: Permission[] = [
  // Sales permissions
  { id: '1', name: 'sales', resource: 'sales', action: 'create' },
  { id: '2', name: 'sales', resource: 'sales', action: 'read' },
  { id: '3', name: 'sales', resource: 'sales', action: 'update' },
  { id: '4', name: 'sales', resource: 'sales', action: 'delete' },
  { id: '5', name: 'sales', resource: 'sales', action: 'export' },
  // Inventory permissions
  { id: '6', name: 'inventory', resource: 'inventory', action: 'create' },
  { id: '7', name: 'inventory', resource: 'inventory', action: 'read' },
  { id: '8', name: 'inventory', resource: 'inventory', action: 'update' },
  { id: '9', name: 'inventory', resource: 'inventory', action: 'delete' },
  { id: '10', name: 'inventory', resource: 'inventory', action: 'export' },
  // Finance permissions
  { id: '11', name: 'finance', resource: 'finance', action: 'create' },
  { id: '12', name: 'finance', resource: 'finance', action: 'read' },
  { id: '13', name: 'finance', resource: 'finance', action: 'update' },
  { id: '14', name: 'finance', resource: 'finance', action: 'delete' },
  { id: '15', name: 'finance', resource: 'finance', action: 'export' },
  // Human Resources permissions
  { id: '16', name: 'hr', resource: 'hr', action: 'create' },
  { id: '17', name: 'hr', resource: 'hr', action: 'read' },
  { id: '18', name: 'hr', resource: 'hr', action: 'update' },
  { id: '19', name: 'hr', resource: 'hr', action: 'delete' },
  { id: '20', name: 'hr', resource: 'hr', action: 'export' },
  // Blockchain Audit permissions
  { id: '21', name: 'blockchain_audit', resource: 'blockchain_audit', action: 'create' },
  { id: '22', name: 'blockchain_audit', resource: 'blockchain_audit', action: 'read' },
  { id: '23', name: 'blockchain_audit', resource: 'blockchain_audit', action: 'update' },
  { id: '24', name: 'blockchain_audit', resource: 'blockchain_audit', action: 'delete' },
  { id: '25', name: 'blockchain_audit', resource: 'blockchain_audit', action: 'export' },
  // Dashboard permissions
  { id: '26', name: 'dashboard', resource: 'dashboard', action: 'read' },
  // Administration permissions for system admin
  { id: '27', name: 'administration', resource: 'administration', action: 'read' },
  { id: '28', name: 'administration', resource: 'administration', action: 'create' },
  { id: '29', name: 'administration', resource: 'administration', action: 'update' },
  { id: '30', name: 'administration', resource: 'administration', action: 'delete' },
  { id: '31', name: 'organizations', resource: 'organizations', action: 'manage' },
  { id: '32', name: 'roles', resource: 'roles', action: 'manage' },
  { id: '33', name: 'users', resource: 'users', action: 'manage' },
];

// Simulate API delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Check if user has a specific permission (e.g., 'sales:create')
export const hasPermission = (userPermissions: string[], requiredPermission: string): boolean => {
  return userPermissions.includes(requiredPermission);
};

// Get user permissions by userId (mock implementation)
// In real backend, this would be an API call: GET /api/users/{userId}/permissions
export const getUserPermissions = async (userId: string): Promise<string[]> => {
  await delay(500); // Simulate network delay

  // Mock logic: different users have different permissions
  // In real app, this would come from backend based on user's roles
  const userPermissionMap: Record<string, string[]> = {
    'user-1': ['sales:read', 'inventory:read', 'dashboard:read'], // Basic user
    'user-2': ['sales:create', 'sales:read', 'sales:update', 'inventory:create', 'inventory:read', 'inventory:update', 'dashboard:read'], // Manager
    'user-3': [ // System Admin - has all permissions including administration
      'sales:create', 'sales:read', 'sales:update', 'sales:delete', 'sales:export',
      'inventory:create', 'inventory:read', 'inventory:update', 'inventory:delete', 'inventory:export',
      'finance:create', 'finance:read', 'finance:update', 'finance:delete', 'finance:export',
      'hr:create', 'hr:read', 'hr:update', 'hr:delete', 'hr:export',
      'blockchain_audit:create', 'blockchain_audit:read', 'blockchain_audit:update', 'blockchain_audit:delete', 'blockchain_audit:export',
      'dashboard:read',
      'administration:read', 'administration:create', 'administration:update', 'administration:delete',
      'organizations:manage',
      'roles:manage',
      'users:manage'
    ],
  };

  return userPermissionMap[userId] || ['dashboard:read']; // Default minimal permissions
};

// Get all available permissions (for matrix display)
export const getAllPermissions = async (): Promise<Permission[]> => {
  await delay(300);
  return mockPermissions;
};

// Get unique resources (for matrix columns)
export const getResources = (): string[] => {
  return Array.from(new Set(mockPermissions.map(p => p.resource)));
};

// Get unique actions (for matrix rows)
export const getActions = (): string[] => {
  return Array.from(new Set(mockPermissions.map(p => p.action)));
};
