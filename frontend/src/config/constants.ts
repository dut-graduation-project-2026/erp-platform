export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    PROFILE: "/auth/profile",
    ORGANIZATIONS: "/auth/organizations",
  },
  SALES: {
    ORDERS: "/sales/orders",
    INVOICES: "/sales/invoices",
  },
  INVENTORY: {
    PRODUCTS: "/inventory/products",
    WAREHOUSES: "/inventory/warehouses",
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
