/**
 * Centralized Permissions Configuration
 * Defines all exact permission string constants matching the Backend database.
 * 
 * Format: {module}:{action}
 * - module: users, roles, organizations, sales, inventory, etc.
 * - action: select, read, write, create, delete, access
 */

export const PERMISSIONS = {
  // --- CORE MODULES (Implemented in Backend) ---
  USERS: {
    SELECT: 'users:select',
    READ: 'users:read',
    WRITE: 'users:write',
    CREATE: 'users:create',
    DELETE: 'users:delete',
  },
  ROLES: {
    SELECT: 'roles:select',
    READ: 'roles:read',
    WRITE: 'roles:write',
    CREATE: 'roles:create',
    DELETE: 'roles:delete',
  },
  ORGANIZATIONS: {
    SELECT: 'organizations:select',
    READ: 'organizations:read',
    WRITE: 'organizations:write',
    CREATE: 'organizations:create',
    DELETE: 'organizations:delete',
  },
  ERP_MODULE: {
    SELECT: 'erp_module:select',
    READ: 'erp_module:read',
    WRITE: 'erp_module:write',
    CREATE: 'erp_module:create',
    DELETE: 'erp_module:delete',
  },

  // --- BUSINESS MODULES (Upcoming / Placeholder) ---
  ANNOUNCEMENT: {
    ACCESS: 'announcement:access',
  },
  ATTENDANCE: {
    ACCESS: 'attendance:access',
  },
  DISCUSS: {
    ACCESS: 'discuss:access',
  },
  CALENDAR: {
    ACCESS: 'calendar:access',
  },
  WORKING_ATTENDANCE: {
    ACCESS: 'working_attendance:access',
  },
  OVER_TIME: {
    ACCESS: 'over_time:access',
  },
  EMPLOYEES: {
    ACCESS: 'employees:access',
  },
  TIME_OFF: {
    ACCESS: 'time_off:access',
  },
  SALES: {
    SELECT: 'sales:select',
    READ: 'sales:read',
    WRITE: 'sales:write',
    CREATE: 'sales:create',
    DELETE: 'sales:delete',
  },
  CRM: {
    SELECT: 'crm:select',
    READ: 'crm:read',
    WRITE: 'crm:write',
    CREATE: 'crm:create',
    DELETE: 'crm:delete',
  },
  INVENTORY: {
    ACCESS: 'inventory:access',
  },
} as const;

// Helper type to get all permission string literal values
export type AppPermission = typeof PERMISSIONS[keyof typeof PERMISSIONS][keyof typeof PERMISSIONS[keyof typeof PERMISSIONS]];
