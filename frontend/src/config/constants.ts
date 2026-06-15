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
    METRICS: (orgId: string, warehouseId: string) => `/organizations/${orgId}/warehouses/${warehouseId}/metrics`,
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
  NOTIFICATIONS: {
    BASE: "/notifications",
    STREAM: "/notifications/stream",
    DELETE: (id: string) => `/notifications/${id}`,
  },
  ANALYTICS: {
    SALES_SUMMARY: (orgId: string) => `/organizations/${orgId}/analytics/sales/summary`,
    SALES_REVENUE_TREND: (orgId: string) => `/organizations/${orgId}/analytics/sales/revenue-trend`,
    SALES_CONVERSION_FUNNEL: (orgId: string) => `/organizations/${orgId}/analytics/sales/conversion-funnel`,
    SALES_TOP_PRODUCTS: (orgId: string) => `/organizations/${orgId}/analytics/sales/top-products`,
    SALES_CATEGORY_DISTRIBUTION: (orgId: string) => `/organizations/${orgId}/analytics/sales/category-distribution`,
    PIPELINE_LEAD_FUNNEL: (orgId: string) => `/organizations/${orgId}/analytics/pipeline/lead-funnel`,
    PIPELINE_SUMMARY: (orgId: string) => `/organizations/${orgId}/analytics/pipeline/summary`,
    INVENTORY_VALUATION_TREND: (orgId: string) => `/organizations/${orgId}/analytics/inventory/valuation-trend`,
    INVENTORY_ASSET_DISTRIBUTION: (orgId: string) => `/organizations/${orgId}/analytics/inventory/asset-distribution`,
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
export const NOTIFICATION_TYPES = {
  INFO: "INFO",
  WARNING: "WARNING",
  ERROR: "ERROR",
  SUCCESS: "SUCCESS",
  ALERT: "ALERT",
} as const;
export type NotificationType = typeof NOTIFICATION_TYPES[keyof typeof NOTIFICATION_TYPES];

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
  WAITING_FOR_STOCK: "WAITING_FOR_STOCK",
} as const;
export type OrderStatus = typeof ORDER_STATUS[keyof typeof ORDER_STATUS];

export const ORDER_STATUS_CONFIG: Record<OrderStatus, { label: string; badgeClass: string }> = {
  [ORDER_STATUS.DRAFT]: {
    label: "DRAFT",
    badgeClass: "bg-gray-50 text-gray-650 border-gray-200",
  },
  [ORDER_STATUS.CONFIRMED]: {
    label: "CONFIRMED",
    badgeClass: "bg-[#f0f4ff] text-[#0066cc] border-[#d0e0ff]",
  },
  [ORDER_STATUS.SENT]: {
    label: "SENT",
    badgeClass: "bg-emerald-50 text-emerald-700 border-emerald-200",
  },
  [ORDER_STATUS.WAITING_FOR_STOCK]: {
    label: "WAITING STOCK",
    badgeClass: "bg-amber-50 text-amber-700 border-amber-200",
  },
  [ORDER_STATUS.COMPLETED]: {
    label: "COMPLETED",
    badgeClass: "bg-emerald-600 text-white border-emerald-650",
  },
  [ORDER_STATUS.CANCELLED]: {
    label: "CANCELLED",
    badgeClass: "bg-red-50 text-red-600 border-red-200",
  },
};

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
