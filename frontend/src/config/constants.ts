export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    PROFILE: "/auth/profile",
  },
  ORGANIZATIONS: {
    BASE: "/organizations",
    ME: "/organizations/me",
    INVITATIONS: (orgId: string) => `/organizations/${orgId}/invitations`,
    ROLES: (orgId: string) => `/organizations/${orgId}/roles`,
  },
  USERS: {
    BASE: "/users",
    ME_PERMISSIONS: "/users/me/permissions",
  },
  ERP_MODULES: {
    BASE: "/erp-modules",
    ME: "/erp-modules/me",
  },
  CRM: {
    LEADS: (orgId: string) => `/organizations/${orgId}/crm/leads`,
    APPOINTMENTS: (orgId: string) => `/organizations/${orgId}/crm/appointments`,
  },
  SALES: {
    ORDERS: (orgId: string) => `/organizations/${orgId}/sale-orders`,
    INVOICES: (orgId: string) => `/organizations/${orgId}/sale-invoices`,
    PARTNERS: (orgId: string) => `/organizations/${orgId}/sale-partners`,
    PRODUCTS: (orgId: string) => `/organizations/${orgId}/products`,
    REPORTS: (orgId: string) => `/organizations/${orgId}/reports/sales-dashboard`,
  },
  BLOCKCHAIN: {
    TRANSACTIONS: "/blockchain/transactions",
  },
}

export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 10,
  MAX_PAGE_SIZE: 100,
}

export const DATE_FORMAT = {
  DISPLAY: "DD/MM/YYYY",
  API: "YYYY-MM-DD",
}

export const CURRENCY = {
  DEFAULT: "VND",
  SYMBOL: "₫",
}

export const PERMISSIONS = {
  SALES_READ: "sales:read",
  SALES_WRITE: "sales:write",
  INVENTORY_READ: "inventory:read",
  INVENTORY_WRITE: "inventory:write",
  BLOCKCHAIN_READ: "blockchain:read",
  BLOCKCHAIN_WRITE: "blockchain:write",
}
