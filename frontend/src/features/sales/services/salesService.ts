import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS, ORDER_STATUS, PARTNER_TYPES } from '@/config/constants';
import {
  SaleOrder,
  SaleInvoice,
  SalePartner,
  Product,
  OrderItem,
  SaleTax,
} from '../types';

export interface PagedEntityResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// ─────────────────────────────────────────────────────────────────────────────
// QUOTATIONS  (DRAFT orders)
// Backend: GET/POST/PUT/DELETE /organizations/{orgId}/quotations
// ─────────────────────────────────────────────────────────────────────────────
export const getQuotations = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PagedEntityResponse<SaleOrder>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleOrder>>(
    API_ENDPOINTS.SALES.QUOTATIONS(orgId),
    { params }
  );
  return response.data;
};

export const getQuotationById = async (orgId: string, id: string): Promise<SaleOrder> => {
  const response = await apiClient.get<SaleOrder>(
    `${API_ENDPOINTS.SALES.QUOTATIONS(orgId)}/${id}`
  );
  return response.data;
};

export const createQuotation = async (
  orgId: string,
  data: { leadId: string; orderNumber?: string; deliveryDate?: string; expirationDate?: string }
): Promise<SaleOrder> => {
  const response = await apiClient.post<SaleOrder>(API_ENDPOINTS.SALES.QUOTATIONS(orgId), data);
  return response.data;
};

export const updateQuotation = async (
  orgId: string,
  id: string,
  data: { leadId: string; orderNumber?: string; deliveryDate?: string; expirationDate?: string }
): Promise<SaleOrder> => {
  const response = await apiClient.put<SaleOrder>(
    `${API_ENDPOINTS.SALES.QUOTATIONS(orgId)}/${id}`,
    data
  );
  return response.data;
};

export const deleteQuotation = async (orgId: string, id: string): Promise<void> => {
  await apiClient.delete(`${API_ENDPOINTS.SALES.QUOTATIONS(orgId)}/${id}`);
};

export const confirmQuotation = async (orgId: string, id: string): Promise<SaleOrder> => {
  const response = await apiClient.patch<SaleOrder>(
    `${API_ENDPOINTS.SALES.QUOTATIONS(orgId)}/${id}/status`,
    { status: ORDER_STATUS.CONFIRMED }
  );
  return response.data;
};

export const cancelQuotation = async (orgId: string, id: string): Promise<SaleOrder> => {
  const response = await apiClient.patch<SaleOrder>(
    `${API_ENDPOINTS.SALES.QUOTATIONS(orgId)}/${id}/status`,
    { status: ORDER_STATUS.CANCELLED }
  );
  return response.data;
};

// Legacy alias so existing imports of `getSaleOrders` don't break
export const getSaleOrders = getQuotations;
export const getSaleOrderById = getQuotationById;
export const createSaleOrder = createQuotation;

// ─────────────────────────────────────────────────────────────────────────────
// CONFIRMED ORDERS  (non-DRAFT)
// Backend: GET/PATCH /organizations/{orgId}/orders
// ─────────────────────────────────────────────────────────────────────────────
export const getOrders = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PagedEntityResponse<SaleOrder>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleOrder>>(
    API_ENDPOINTS.SALES.ORDERS(orgId),
    { params }
  );
  return response.data;
};

export const getOrderById = async (orgId: string, id: string): Promise<SaleOrder> => {
  const response = await apiClient.get<SaleOrder>(
    `${API_ENDPOINTS.SALES.ORDERS(orgId)}/${id}`
  );
  return response.data;
};

export const updateOrderStatus = async (
  orgId: string,
  id: string,
  status: 'CONFIRMED' | 'CANCELLED'
): Promise<SaleOrder> => {
  const response = await apiClient.patch<SaleOrder>(
    `${API_ENDPOINTS.SALES.ORDERS(orgId)}/${id}/status`,
    { status }
  );
  return response.data;
};

// ─────────────────────────────────────────────────────────────────────────────
// ORDER ITEMS  (lines of a quotation/order)
// Backend: GET/POST/PUT/DELETE /organizations/{orgId}/orders/{orderId}/items
// ─────────────────────────────────────────────────────────────────────────────
const orderItemsUrl = (orgId: string, orderId: string) =>
  `/organizations/${orgId}/orders/${orderId}/items`;

export const getOrderItems = async (orgId: string, orderId: string): Promise<OrderItem[]> => {
  const response = await apiClient.get<OrderItem[]>(orderItemsUrl(orgId, orderId));
  return response.data;
};

export const createOrderItem = async (
  orgId: string,
  orderId: string,
  data: { productId: string; taxId?: string; quantity: number; unitPrice: number }
): Promise<OrderItem> => {
  const response = await apiClient.post<OrderItem>(orderItemsUrl(orgId, orderId), data);
  return response.data;
};

export const updateOrderItem = async (
  orgId: string,
  orderId: string,
  itemId: string,
  data: { productId: string; taxId?: string; quantity: number; unitPrice: number }
): Promise<OrderItem> => {
  const response = await apiClient.put<OrderItem>(
    `${orderItemsUrl(orgId, orderId)}/${itemId}`,
    data
  );
  return response.data;
};

export const deleteOrderItem = async (
  orgId: string,
  orderId: string,
  itemId: string
): Promise<void> => {
  await apiClient.delete(`${orderItemsUrl(orgId, orderId)}/${itemId}`);
};

// ─────────────────────────────────────────────────────────────────────────────
// TAXES
// Backend: /organizations/{orgId}/taxes
// ─────────────────────────────────────────────────────────────────────────────
export const getTaxes = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number; isArchived?: boolean }
): Promise<PagedEntityResponse<SaleTax>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleTax>>(
    API_ENDPOINTS.SALES.TAXES(orgId),
    { params }
  );
  return response.data;
};

export const createTax = async (orgId: string, data: Partial<SaleTax>): Promise<SaleTax> => {
  const response = await apiClient.post<SaleTax>(API_ENDPOINTS.SALES.TAXES(orgId), data);
  return response.data;
};

export const updateTax = async (orgId: string, id: string, data: Partial<SaleTax>): Promise<SaleTax> => {
  const response = await apiClient.put<SaleTax>(`${API_ENDPOINTS.SALES.TAXES(orgId)}/${id}`, data);
  return response.data;
};

export const deleteTax = async (orgId: string, id: string): Promise<void> => {
  await apiClient.delete(`${API_ENDPOINTS.SALES.TAXES(orgId)}/${id}`);
};

// ─────────────────────────────────────────────────────────────────────────────
// PARTNERS  (Customers / Vendors)
// Backend: /organizations/{orgId}/partners
// ─────────────────────────────────────────────────────────────────────────────
export const getPartners = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number; isArchived?: boolean }
): Promise<PagedEntityResponse<SalePartner>> => {
  const response = await apiClient.get<SalePartner[]>(
    API_ENDPOINTS.SALES.PARTNERS(orgId),
    { params }
  );
  
  const list = Array.isArray(response.data) ? response.data : [];
  const mapped = list.map(p => ({
    ...p,
    type: p.partnerType || PARTNER_TYPES.INDIVIDUAL,
    partnerType: p.partnerType || PARTNER_TYPES.INDIVIDUAL
  }));

  return {
    data: mapped,
    total: mapped.length,
    page: 1,
    limit: mapped.length,
    totalPages: 1
  };
};

export const getPartnerById = async (orgId: string, id: string): Promise<SalePartner> => {
  const response = await apiClient.get<SalePartner>(
    `${API_ENDPOINTS.SALES.PARTNERS(orgId)}/${id}`
  );
  return {
    ...response.data,
    type: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL,
    partnerType: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL
  };
};

export const createPartner = async (orgId: string, data: Partial<SalePartner>): Promise<SalePartner> => {
  let pType = data.partnerType || data.type || PARTNER_TYPES.INDIVIDUAL;
  if (pType === 'CUSTOMER') pType = PARTNER_TYPES.INDIVIDUAL;
  if (pType === 'VENDOR') pType = PARTNER_TYPES.COMPANY;

  const { type, code, ...payload } = data;
  const response = await apiClient.post<SalePartner>(
    API_ENDPOINTS.SALES.PARTNERS(orgId), 
    { ...payload, partnerType: pType }
  );
  return {
    ...response.data,
    type: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL,
    partnerType: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL
  };
};

export const updatePartner = async (
  orgId: string,
  id: string,
  data: Partial<SalePartner>
): Promise<SalePartner> => {
  let pType = data.partnerType || data.type || PARTNER_TYPES.INDIVIDUAL;
  if (pType === 'CUSTOMER') pType = PARTNER_TYPES.INDIVIDUAL;
  if (pType === 'VENDOR') pType = PARTNER_TYPES.COMPANY;

  const { type, code, ...payload } = data;
  const response = await apiClient.put<SalePartner>(
    `${API_ENDPOINTS.SALES.PARTNERS(orgId)}/${id}`,
    { ...payload, partnerType: pType }
  );
  return {
    ...response.data,
    type: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL,
    partnerType: response.data.partnerType || PARTNER_TYPES.INDIVIDUAL
  };
};

// ─────────────────────────────────────────────────────────────────────────────
// PRODUCTS
// Backend: /organizations/{orgId}/products
// ─────────────────────────────────────────────────────────────────────────────
export const getProducts = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number; isArchived?: boolean }
): Promise<PagedEntityResponse<Product>> => {
  const response = await apiClient.get<PagedEntityResponse<Product>>(
    API_ENDPOINTS.SALES.PRODUCTS(orgId),
    { params }
  );
  // Normalise `salePrice` → `price` so existing UI doesn't break
  if (response.data?.data) {
    response.data.data = response.data.data.map((p: any) => ({
      ...p,
      price: p.price ?? p.salePrice ?? 0,
      sku: p.sku ?? p.code,
    }));
  }
  return response.data;
};

export const getProductById = async (orgId: string, id: string): Promise<Product> => {
  const response = await apiClient.get<Product>(`${API_ENDPOINTS.SALES.PRODUCTS(orgId)}/${id}`);
  const p = response.data as any;
  return { ...p, price: p.price ?? p.salePrice ?? 0, sku: p.sku ?? p.code };
};


// ─── INVOICES ────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
export const getSaleInvoices = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PagedEntityResponse<SaleInvoice>> => {
  const response = await apiClient.get<PagedEntityResponse<any>>(
    API_ENDPOINTS.SALES.INVOICES(orgId),
    { params }
  );
  return {
    ...response.data,
    data: (response.data.data || []).map((inv: any) => ({
      ...inv,
      saleOrder: inv.order
    }))
  };
};

export const getSaleInvoiceById = async (orgId: string, id: string): Promise<SaleInvoice> => {
  const response = await apiClient.get<Omit<SaleInvoice, 'saleOrder'> & { order: SaleOrder }>(
    `${API_ENDPOINTS.SALES.INVOICES(orgId)}/${id}`
  );
  return { 
    ...response.data, 
    saleOrder: response.data.order 
  };
};

export const createInvoice = async (
  orgId: string,
  data: { orderId: string; dueDate?: string }
): Promise<SaleInvoice> => {
  const response = await apiClient.post<SaleInvoice>(
    API_ENDPOINTS.SALES.INVOICES(orgId),
    data
  );
  return response.data;
};

export const updateInvoiceStatus = async (
  orgId: string,
  id: string,
  status: string
): Promise<SaleInvoice> => {
  const response = await apiClient.patch<SaleInvoice>(
    `${API_ENDPOINTS.SALES.INVOICES(orgId)}/${id}/status`,
    { status }
  );
  return response.data;
};

export const registerPayment = async (
  orgId: string,
  id: string,
  amount: number
): Promise<SaleInvoice> => {
  const response = await apiClient.post<SaleInvoice>(
    `${API_ENDPOINTS.SALES.INVOICES(orgId)}/${id}/payments`,
    { amount }
  );
  return response.data;
};

// ─────────────────────────────────────────────────────────────────────────────
// ANALYTICS
// ─────────────────────────────────────────────────────────────────────────────
export interface SalesSummaryResponse {
  ytdNetRevenue: number;
  avgDealSize: number;
  activeSalesReps: number;
  previousPeriodRevenue: number;
  revenueGrowthPercent: number;
}

export interface RevenueTrendPoint {
  date: string;
  grossSales: number;
  cogs: number;
  netMargin: number;
}

export interface OrderStatusCount {
  status: string;
  count: number;
}

export interface TopProductResponse {
  productId: string;
  productName: string;
  categoryName: string;
  totalRevenue: number;
  quantitySold: number;
  orderCount: number;
}

export interface CategorySalesDistribution {
  categoryId: string;
  categoryName: string;
  totalRevenue: number;
  percentage: number;
  quantitySold: number;
  orderCount: number;
}

export const getSalesSummary = async (
  orgId: string,
  params?: { periodType?: string; year?: number }
): Promise<SalesSummaryResponse> => {
  const response = await apiClient.get<SalesSummaryResponse>(
    API_ENDPOINTS.ANALYTICS.SALES_SUMMARY(orgId),
    { params }
  );
  return response.data;
};

export const getRevenueTrend = async (
  orgId: string,
  params?: { months?: number; year?: number }
): Promise<RevenueTrendPoint[]> => {
  const response = await apiClient.get<RevenueTrendPoint[]>(
    API_ENDPOINTS.ANALYTICS.SALES_REVENUE_TREND(orgId),
    { params }
  );
  return response.data;
};

export const getSalesConversionFunnel = async (
  orgId: string,
  params?: { periodType?: string; year?: number }
): Promise<OrderStatusCount[]> => {
  const response = await apiClient.get<OrderStatusCount[]>(
    API_ENDPOINTS.ANALYTICS.SALES_CONVERSION_FUNNEL(orgId),
    { params }
  );
  return response.data;
};

export const getSalesTopProducts = async (
  orgId: string,
  params?: { limit?: number; startDate?: string; endDate?: string }
): Promise<TopProductResponse[]> => {
  const response = await apiClient.get<TopProductResponse[]>(
    API_ENDPOINTS.ANALYTICS.SALES_TOP_PRODUCTS(orgId),
    { params }
  );
  return response.data;
};

export const getSalesCategoryDistribution = async (
  orgId: string,
  params?: { startDate?: string; endDate?: string }
): Promise<CategorySalesDistribution[]> => {
  const response = await apiClient.get<CategorySalesDistribution[]>(
    API_ENDPOINTS.ANALYTICS.SALES_CATEGORY_DISTRIBUTION(orgId),
    { params }
  );
  return response.data;
};

export interface ProductCategory {
  id: string;
  name: string;
  description?: string;
}

export const getProductCategories = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PagedEntityResponse<ProductCategory>> => {
  const response = await apiClient.get<PagedEntityResponse<ProductCategory>>(
    `/organizations/${orgId}/product-categories`,
    { params }
  );
  return response.data;
};
// ─────────────────────────────────────────────────────────────────────────────
// ACTIONABLE AI: SALES FORECASTING
// ─────────────────────────────────────────────────────────────────────────────
export interface AiForecastPoint {
  date: string;
  historical_revenue: number | null;
  predicted_revenue: number;
}

export interface AiSalesForecastResponse {
  summary: string;
  forecast_30d_total_revenue: number;
  forecast_points: AiForecastPoint[];
  insights: string[];
}

export const getAiSalesForecast = async (
  orgId: string,
  period: string = '30d'
): Promise<AiSalesForecastResponse> => {
  const response = await apiClient.get<AiSalesForecastResponse>(
    `/organizations/${orgId}/ai/sales/forecast`,
    { params: { period } }
  );
  return response.data;
};

