import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import { SaleOrder, SaleInvoice, SalePartner, Product } from '../types';

export interface PagedEntityResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

// Sale Orders
export const getSaleOrders = async (orgId: string, params?: any): Promise<PagedEntityResponse<SaleOrder>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleOrder>>(API_ENDPOINTS.SALES.ORDERS(orgId), { params });
  return response.data;
};

export const getSaleOrderById = async (orgId: string, id: string): Promise<SaleOrder> => {
  const response = await apiClient.get<SaleOrder>(`${API_ENDPOINTS.SALES.ORDERS(orgId)}/${id}`);
  return response.data;
};

export const createSaleOrder = async (orgId: string, data: any): Promise<SaleOrder> => {
  const response = await apiClient.post<SaleOrder>(API_ENDPOINTS.SALES.ORDERS(orgId), data);
  return response.data;
};

// Sale Invoices
export const getSaleInvoices = async (orgId: string, params?: any): Promise<PagedEntityResponse<SaleInvoice>> => {
  const response = await apiClient.get<PagedEntityResponse<SaleInvoice>>(API_ENDPOINTS.SALES.INVOICES(orgId), { params });
  return response.data;
};

export const getSaleInvoiceById = async (orgId: string, id: string): Promise<SaleInvoice> => {
  const response = await apiClient.get<SaleInvoice>(`${API_ENDPOINTS.SALES.INVOICES(orgId)}/${id}`);
  return response.data;
};

// Partners
export const getPartners = async (orgId: string, params?: any): Promise<PagedEntityResponse<SalePartner>> => {
  const response = await apiClient.get<PagedEntityResponse<SalePartner>>(API_ENDPOINTS.SALES.PARTNERS(orgId), { params });
  return response.data;
};

export const createPartner = async (orgId: string, data: any): Promise<SalePartner> => {
  const response = await apiClient.post<SalePartner>(API_ENDPOINTS.SALES.PARTNERS(orgId), data);
  return response.data;
};

export const updatePartner = async (orgId: string, id: string, data: any): Promise<SalePartner> => {
  const response = await apiClient.put<SalePartner>(`${API_ENDPOINTS.SALES.PARTNERS(orgId)}/${id}`, data);
  return response.data;
};

// Products
export const getProducts = async (orgId: string, params?: any): Promise<PagedEntityResponse<Product>> => {
  const response = await apiClient.get<PagedEntityResponse<Product>>(API_ENDPOINTS.SALES.PRODUCTS(orgId), { params });
  return response.data;
};

// Analytics Dashboard
export const getSalesDashboard = async (orgId: string): Promise<any> => {
  const response = await apiClient.get(API_ENDPOINTS.SALES.REPORTS(orgId));
  return response.data;
};
