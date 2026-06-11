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

  PARTNERS: {
    SELECT: 'partners:select',
    READ: 'partners:read',
    WRITE: 'partners:write',
    CREATE: 'partners:create',
    DELETE: 'partners:delete',
  },
  PRODUCTS: {
    SELECT: 'products:select',
    READ: 'products:read',
    WRITE: 'products:write',
    CREATE: 'products:create',
    DELETE: 'products:delete',
  },
  SALE_TEAMS: {
    SELECT: 'sale_teams:select',
    READ: 'sale_teams:read',
    WRITE: 'sale_teams:write',
    CREATE: 'sale_teams:create',
    DELETE: 'sale_teams:delete',
  },
  LEADS: {
    SELECT: 'leads:select',
    READ: 'leads:read',
    WRITE: 'leads:write',
    CREATE: 'leads:create',
    DELETE: 'leads:delete',
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
    SELECT: 'partners:select',
    READ: 'partners:read',
    WRITE: 'partners:write',
    CREATE: 'partners:create',
    DELETE: 'partners:delete',
  },
  CRM: {
    SELECT: 'leads:select',
    READ: 'leads:read',
    WRITE: 'leads:write',
    CREATE: 'leads:create',
    DELETE: 'leads:delete',
  },
  ORDERS: {
    SELECT: 'orders:select',
    READ: 'orders:read',
    WRITE: 'orders:write',
    CREATE: 'orders:create',
    DELETE: 'orders:delete',
  },
  INVOICES: {
    SELECT: 'invoices:select',
    READ: 'invoices:read',
    WRITE: 'invoices:write',
    CREATE: 'invoices:create',
    DELETE: 'invoices:delete',
  },
  TAXES: {
    SELECT: 'taxes:select',
    READ: 'taxes:read',
    WRITE: 'taxes:write',
    CREATE: 'taxes:create',
    DELETE: 'taxes:delete',
  },
  INVENTORY: {
    ACCESS: 'warehouses:read',
  },
  WAREHOUSES: {
    SELECT: 'warehouses:select',
    READ: 'warehouses:read',
    WRITE: 'warehouses:write',
    CREATE: 'warehouses:create',
    DELETE: 'warehouses:delete',
  },
  INVENTORY_DOCUMENTS: {
    SELECT: 'inventory-documents:select',
    READ: 'inventory-documents:read',
    WRITE: 'inventory-documents:write',
    CREATE: 'inventory-documents:create',
    DELETE: 'inventory-documents:delete',
  },
  INVENTORY_TRANSACTIONS: {
    SELECT: 'inventory-transactions:select',
    READ: 'inventory-transactions:read',
    WRITE: 'inventory-transactions:write',
    CREATE: 'inventory-transactions:create',
    DELETE: 'inventory-transactions:delete',
  },
  REPLENISHMENT_REQUESTS: {
    SELECT: 'replenishment-requests:select',
    READ: 'replenishment-requests:read',
    WRITE: 'replenishment-requests:write',
    CREATE: 'replenishment-requests:create',
    DELETE: 'replenishment-requests:delete',
  },
  STOCK_VALUATIONS: {
    SELECT: 'stock-valuations:select',
    READ: 'stock-valuations:read',
    WRITE: 'stock-valuations:write',
    CREATE: 'stock-valuations:create',
    DELETE: 'stock-valuations:delete',
  },
} as const;

// Helper type to get all permission string literal values
export type AppPermission = typeof PERMISSIONS[keyof typeof PERMISSIONS][keyof typeof PERMISSIONS[keyof typeof PERMISSIONS]];
