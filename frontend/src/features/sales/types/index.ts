import { 
  PartnerType, 
  OrderStatus, 
  TaxComputation, 
  InvoiceStatus 
} from "@/config/constants";

export interface PartnerContact {
  id?: string;
  name: string;
  email?: string;
  phone?: string;
  jobPosition?: string;
  notes?: string;
}

// ─── Partner (Khách hàng / Nhà cung cấp) ──────────────────────────────────
export interface SalePartner {
  id: string;
  code?: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  taxCode?: string;
  type: PartnerType | 'CUSTOMER' | 'VENDOR'; // Union kept for legacy frontend support
  partnerType?: PartnerType;
  isArchived?: boolean;
  contacts?: PartnerContact[];
}

export interface ProductCategory {
  id: string;
  name: string;
  description?: string;
}

// ─── Product ──────────────────────────────────────────────────────────────
export interface Product {
  id: string;
  name: string;
  code: string;   // backend field
  sku: string;   // alias kept for legacy UI
  description: string;
  price: number;  // frontend alias; backend sends `salePrice`
  salePrice?: number;
  purchasePrice?: number;
  isActive?: boolean;
  isArchived?: boolean;
  categoryId?: string;
  category?: ProductCategory;
  image?: string;
}

// ─── Tax ──────────────────────────────────────────────────────────────────
export interface SaleTax {
  id: string;
  name: string;
  computation: TaxComputation;
  amount: number;
  description?: string;
  isArchived?: boolean;
}

// ─── Order Item (line) ────────────────────────────────────────────────────
export interface OrderItem {
  id?: string;
  organizationId?: string;
  orderId?: string;
  productId: string;
  product?: Product;
  taxId?: string;
  tax?: SaleTax;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  // Legacy fields kept for UI compat
  description?: string;
  taxPercentage?: number;
}

// ─── Order / Quotation ────────────────────────────────────────────────────
export interface SaleOrder {
  id: string;
  orderNumber: string;
  // legacy alias used in the old UI
  code?: string;
  partner?: SalePartner;
  lead?: {
    id: string;
    name: string;
    taxCode?: string;
    email?: string;
    phone?: string;
    expectedRevenue?: number;
    stage?: string;
    probability?: number;
    salePerson?: {
      id: string;
      email: string;
      firstName: string;
      lastName: string;
    };
    saleTeam?: {
      id: string;
      name: string;
    };
    partner?: SalePartner;
    createdAt?: string;
    updatedAt?: string;
  };
  status: OrderStatus;
  deliveryDate?: string;
  expirationDate?: string;
  totalAmount: number;
  items?: OrderItem[];
  createdAt?: string;
  updatedAt?: string;
  organization?: {
    id: string;
    name: string;
  };
  createdBy?: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  updatedBy?: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  // Legacy fields used in old SaleOrderForm UI
  orderDate?: string;
  lines?: OrderItem[];
  termsAndConditions?: string;
  taxAmount?: number;
  netAmount?: number;
  warehouseId?: string;
  warehouseName?: string;
  invoiceId?: string;
  invoiceNumber?: string;
  invoiceStatus?: InvoiceStatus;
}

// ─── Invoice (Tích hợp thực tế backend) ───────────────────────────
export interface SaleInvoice {
  id: string;
  invoiceNumber: string;
  code?: string; // Kept for legacy compatibility
  saleOrder?: SaleOrder;
  partner?: SalePartner;
  invoiceDate?: string; // Kept for legacy compatibility
  dueDate: string;
  status: InvoiceStatus;
  totalAmount: number;
  paidAmount: number;
  amountDue?: number;
  createdAt?: string;
  updatedAt?: string;
  organization?: {
    id: string;
    name: string;
  };
  createdBy?: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  updatedBy?: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
}

