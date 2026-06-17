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

// Define groups for permission matrices (Role edit form page, RoleMatrix, RolePermissionMatrix)
export const PERMISSION_GROUPS = [
  {
    id: "crm",
    name: "CRM",
    resources: ["leads", "dashboard"]
  },
  {
    id: "sales",
    name: "Bán hàng",
    resources: [
      "partners",
      "sale_teams",
      "orders",
      "invoices",
      "products",
      "taxes",
      "product_categories",
      "sales"
    ]
  },
  {
    id: "inventory",
    name: "Kho & Chuỗi cung ứng",
    resources: [
      "warehouses",
      "inventory-documents",
      "inventory-transactions",
      "replenishment-requests",
      "stock-valuations",
      "inventory",
      "finance",
      "hr",
      "blockchain_audit"
    ]
  },
  {
    id: "admin",
    name: "Quản trị Hệ thống",
    resources: [
      "users",
      "roles",
      "organizations",
      "administration"
    ]
  }
];

export const RESOURCE_LABELS: Record<string, string> = {
  // CRM
  'leads': 'Cơ hội kinh doanh (CRM Leads)',
  'dashboard': 'Bảng thông tin chính (Dashboard)',
  
  // Sales
  'partners': 'Khách hàng & Nhà cung cấp (Partners)',
  'sale_teams': 'Đội ngũ bán hàng (Sale Teams)',
  'orders': 'Đơn bán hàng (Orders)',
  'invoices': 'Hóa đơn (Invoices)',
  'products': 'Sản phẩm (Products)',
  'taxes': 'Thuế suất (Taxes)',
  'product_categories': 'Nhóm sản phẩm (Product Categories)',
  'sales': 'Phân hệ Bán hàng (Sales)',
  
  // Inventory
  'warehouses': 'Kho hàng (Warehouses)',
  'inventory-documents': 'Phiếu kho (Inventory Documents)',
  'inventory-transactions': 'Giao dịch kho (Inventory Transactions)',
  'replenishment-requests': 'Yêu cầu cung ứng (Replenishment Requests)',
  'stock-valuations': 'Định giá tồn kho (Stock Valuations)',
  'inventory': 'Phân hệ Kho hàng (Inventory)',
  'finance': 'Tài chính & Kế toán (Finance)',
  'hr': 'Quản trị nhân sự (HR)',
  'blockchain_audit': 'Kiểm toán Blockchain (Blockchain Audit)',
  
  // Administration
  'users': 'Quản lý Người dùng (Users)',
  'roles': 'Vai trò & Phân quyền (Roles)',
  'organizations': 'Thông tin Tổ chức (Organizations)',
  'administration': 'Quản trị Hệ thống (Administration)',
};

// Map high-level backend module codes to resource prefixes for Launcher hasModuleAccess filter
export const MODULE_PERMISSIONS_MAP: Record<string, string[]> = {
  leads: ["leads"],
  partners: ["partners", "sale_teams", "orders", "invoices", "products", "taxes", "product_categories"],
  warehouses: ["warehouses", "inventory-documents", "inventory-transactions", "replenishment-requests", "stock-valuations"],
  users: ["users"],
  roles: ["roles"],
  organizations: ["organizations"],
  erp_module_v1: ["erp_module"]
};

// Actions definition for role permission matrices
export const MATRIX_ACTIONS = [
  { key: 'READ', label: 'Xem', icon: '👁️' },
  { key: 'CREATE', label: 'Tạo', icon: '➕' },
  { key: 'UPDATE', label: 'Sửa', icon: '✏️' },
  { key: 'DELETE', label: 'Xóa', icon: '🗑️' },
  { key: 'EXPORT', label: 'Xuất', icon: '📥' },
];

export const BACKEND_ACTIONS = [
  { key: 'read', label: 'Xem (Read)' },
  { key: 'select', label: 'Chọn (Select)' },
  { key: 'create', label: 'Tạo (Create)' },
  { key: 'write', label: 'Sửa (Write)' },
  { key: 'delete', label: 'Xóa (Delete)' },
];
