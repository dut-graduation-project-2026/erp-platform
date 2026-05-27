export interface SalePartner {
  id: string;
  code: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  taxCode?: string;
  type: 'CUSTOMER' | 'VENDOR';
}

export interface Product {
  id: string;
  name: string;
  sku: string;
  description: string;
  price: number;
  isActive?: boolean;
}

export interface SaleOrderLine {
  id?: string;
  productId: string;
  product?: Product;
  description: string;
  quantity: number;
  unitPrice: number;
  taxPercentage: number;
  subtotal: number;
}

export interface SaleOrder {
  id: string;
  code: string;
  partner: SalePartner;
  orderDate: string;
  status: 'DRAFT' | 'SENT' | 'CONFIRMED' | 'CANCELLED';
  lines: SaleOrderLine[];
  totalAmount: number;
  taxAmount: number;
  netAmount: number;
  termsAndConditions?: string;
}

export interface SaleInvoice {
  id: string;
  code: string;
  saleOrder?: SaleOrder;
  partner: SalePartner;
  invoiceDate: string;
  dueDate: string;
  status: 'DRAFT' | 'POSTED' | 'PAID' | 'CANCELLED';
  totalAmount: number;
  amountDue: number;
}
