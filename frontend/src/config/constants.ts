export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    PROFILE: "/auth/profile",
    FORGOT_PASSWORD: "/auth/forgot-password",
    RESET_PASSWORD: "/auth/reset-password",
  },
  ORGANIZATIONS: {
    BASE: "/organizations",
    ME: "/organizations/me",
    INVITATIONS: (orgId: string) => `/organizations/${orgId}/invitations`,
    INVITATION_DETAIL: (orgId: string, invitationId: string) => `/organizations/${orgId}/invitations/${invitationId}`,
    ROLES: (orgId: string) => `/organizations/${orgId}/roles`,
  },
  USERS: {
    BASE: "/users",
    DETAIL: (userId: string) => `/users/${userId}`,
    ROLES: (userId: string) => `/users/${userId}/roles`,
  },
  ERP_MODULES: {
    BASE: "/erp-modules",
    ME: "/erp-modules/me",
  },
  CRM: {
    LEADS: (orgId: string) => `/organizations/${orgId}/leads`,
    APPOINTMENTS: (orgId: string) => `/organizations/${orgId}/crm/appointments`,
    SALE_TEAMS: (orgId: string) => `/organizations/${orgId}/sale-teams`,
  },
  SALES: {
    QUOTATIONS: (orgId: string) => `/organizations/${orgId}/quotations`,
    ORDERS: (orgId: string) => `/organizations/${orgId}/orders`,
    INVOICES: (orgId: string) => `/organizations/${orgId}/invoices`,
    PARTNERS: (orgId: string) => `/organizations/${orgId}/partners`,
    PRODUCTS: (orgId: string) => `/organizations/${orgId}/products`,
    TAXES: (orgId: string) => `/organizations/${orgId}/taxes`,
    REPORTS: (orgId: string) => `/organizations/${orgId}/reports/sales-dashboard`,
  },
  INVENTORY: {
    WAREHOUSES: (orgId: string) => `/organizations/${orgId}/warehouses`,
    BALANCES: (orgId: string, warehouseId: string) => `/organizations/${orgId}/warehouses/${warehouseId}/balances`,
    DOCUMENTS: (orgId: string, warehouseId: string) => `/organizations/${orgId}/warehouses/${warehouseId}/documents`,
    REPLENISHMENT_REQUESTS: (orgId: string, warehouseId: string) => `/organizations/${orgId}/warehouses/${warehouseId}/replenishment-requests`,
    COGS: (orgId: string, orderId: string) => `/organizations/${orgId}/orders/${orderId}/cogs`,
  },
  BLOCKCHAIN: {
    TRANSACTIONS: "/blockchain/transactions",
  },
  PUBLIC: {
    INVITATION_DETAIL: (orgId: string, invitationId: string) => `/public/organizations/${orgId}/invitations/${invitationId}`,
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

// ─── ERP System Enums & Types ──────────────────────────────────────────────
export const PARTNER_TYPES = {
  INDIVIDUAL: "INDIVIDUAL",
  COMPANY: "COMPANY",
} as const;
export type PartnerType = typeof PARTNER_TYPES[keyof typeof PARTNER_TYPES];

export const ORDER_STATUS = {
  DRAFT: "DRAFT",
  SENT: "SENT",
  CONFIRMED: "CONFIRMED",
  CANCELLED: "CANCELLED",
  COMPLETED: "COMPLETED",
} as const;
export type OrderStatus = typeof ORDER_STATUS[keyof typeof ORDER_STATUS];

export const TAX_COMPUTATION = {
  PERCENTAGE: "PERCENTAGE",
  FIXED_AMOUNT: "FIXED_AMOUNT",
} as const;
export type TaxComputation = typeof TAX_COMPUTATION[keyof typeof TAX_COMPUTATION];

export const LEAD_STAGE = {
  NEW: "NEW",
  QUALIFIED: "QUALIFIED",
  PROPOSAL: "PROPOSAL",
  WON: "WON",
  LOST: "LOST",
} as const;
export type LeadStage = typeof LEAD_STAGE[keyof typeof LEAD_STAGE];

export const INVITATION_STATUS = {
  PENDING: "PENDING",
  ACCEPTED: "ACCEPTED",
  DECLINED: "DECLINED",
  EXPIRED: "EXPIRED",
  REVOKED: "REVOKED",
} as const;
export type InvitationStatus = typeof INVITATION_STATUS[keyof typeof INVITATION_STATUS];

export const INVOICE_STATUS = {
  DRAFT: "DRAFT",
  POSTED: "POSTED",
  PAID: "PAID",
  PARTIAL_PAID: "PARTIAL_PAID",
  CANCELLED: "CANCELLED",
} as const;
export type InvoiceStatus = typeof INVOICE_STATUS[keyof typeof INVOICE_STATUS];

// ─── INVENTORY ENUMS & TYPES ────────────────────────────────────────────────
export const DOCUMENT_TYPE = {
  RECEIPT: "RECEIPT",
  ISSUE: "ISSUE",
  ADJUSTMENT: "ADJUSTMENT",
  TRANSFER_IN: "TRANSFER_IN",
  TRANSFER_OUT: "TRANSFER_OUT",
} as const;
export type DocumentType = typeof DOCUMENT_TYPE[keyof typeof DOCUMENT_TYPE];

export const REFERENCE_TYPE = {
  PURCHASE_ORDER: "PURCHASE_ORDER",
  SALES_ORDER: "SALES_ORDER",
  INVENTORY_COUNT: "INVENTORY_COUNT",
  MANUAL: "MANUAL",
} as const;
export type ReferenceType = typeof REFERENCE_TYPE[keyof typeof REFERENCE_TYPE];

export const DOCUMENT_STATUS = {
  DRAFT: "DRAFT",
  CONFIRMED: "CONFIRMED",
  COMPLETED: "COMPLETED",
  CANCELLED: "CANCELLED",
  WAITING_FOR_STOCK: "WAITING_FOR_STOCK",
} as const;
export type DocumentStatus = typeof DOCUMENT_STATUS[keyof typeof DOCUMENT_STATUS];

export const COGS_METHOD = {
  FIFO: "FIFO",
  LIFO: "LIFO",
  AVERAGE: "AVERAGE",
} as const;
export type CogsMethod = typeof COGS_METHOD[keyof typeof COGS_METHOD];

