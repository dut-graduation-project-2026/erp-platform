import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import { PaginatedResponse } from '@/types/common';
import {
  Warehouse,
  CreateWarehouseRequest,
  InventoryBalance,
  InventoryDocument,
  CreateInventoryDocumentRequest,
  ReplenishmentRequest,
  CreateReplenishmentRequest,
  StockValuation,
} from '../types';

// ─── WAREHOUSE CRUD ──────────────────────────────────────────────────────────

export const getWarehouses = async (
  orgId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PaginatedResponse<Warehouse>> => {
  const response = await apiClient.get<PaginatedResponse<Warehouse>>(
    API_ENDPOINTS.INVENTORY.WAREHOUSES(orgId),
    { params }
  );
  return response.data;
};

export const getWarehouseById = async (orgId: string, id: string): Promise<Warehouse> => {
  const response = await apiClient.get<Warehouse>(
    `${API_ENDPOINTS.INVENTORY.WAREHOUSES(orgId)}/${id}`
  );
  return response.data;
};

export const createWarehouse = async (
  orgId: string,
  data: CreateWarehouseRequest
): Promise<Warehouse> => {
  const response = await apiClient.post<Warehouse>(
    API_ENDPOINTS.INVENTORY.WAREHOUSES(orgId),
    data
  );
  return response.data;
};

export const updateWarehouse = async (
  orgId: string,
  id: string,
  data: Partial<CreateWarehouseRequest>
): Promise<Warehouse> => {
  const response = await apiClient.put<Warehouse>(
    `${API_ENDPOINTS.INVENTORY.WAREHOUSES(orgId)}/${id}`,
    data
  );
  return response.data;
};

export const deleteWarehouse = async (orgId: string, id: string): Promise<void> => {
  await apiClient.delete(`${API_ENDPOINTS.INVENTORY.WAREHOUSES(orgId)}/${id}`);
};

// ─── INVENTORY BALANCES ──────────────────────────────────────────────────────

export const getInventoryBalances = async (
  orgId: string,
  warehouseId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PaginatedResponse<InventoryBalance>> => {
  const response = await apiClient.get<PaginatedResponse<InventoryBalance>>(
    API_ENDPOINTS.INVENTORY.BALANCES(orgId, warehouseId),
    { params }
  );
  return response.data;
};

// ─── INVENTORY DOCUMENTS ─────────────────────────────────────────────────────

export const getInventoryDocuments = async (
  orgId: string,
  warehouseId: string,
  params?: { search?: string; page?: number; limit?: number }
): Promise<PaginatedResponse<InventoryDocument>> => {
  const response = await apiClient.get<PaginatedResponse<InventoryDocument>>(
    API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId),
    { params }
  );
  return response.data;
};

export const getInventoryDocumentById = async (
  orgId: string,
  warehouseId: string,
  documentId: string
): Promise<InventoryDocument> => {
  const response = await apiClient.get<InventoryDocument>(
    `${API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId)}/${documentId}`
  );
  return response.data;
};

export const createInventoryDocument = async (
  orgId: string,
  warehouseId: string,
  data: CreateInventoryDocumentRequest
): Promise<InventoryDocument> => {
  const response = await apiClient.post<InventoryDocument>(
    API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId),
    data
  );
  return response.data;
};

export const confirmInventoryDocument = async (
  orgId: string,
  warehouseId: string,
  documentId: string
): Promise<InventoryDocument> => {
  const response = await apiClient.post<InventoryDocument>(
    `${API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId)}/${documentId}/confirm`
  );
  return response.data;
};

export const completeInventoryDocument = async (
  orgId: string,
  warehouseId: string,
  documentId: string
): Promise<InventoryDocument> => {
  const response = await apiClient.post<InventoryDocument>(
    `${API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId)}/${documentId}/complete`
  );
  return response.data;
};

export const cancelInventoryDocument = async (
  orgId: string,
  warehouseId: string,
  documentId: string
): Promise<InventoryDocument> => {
  const response = await apiClient.post<InventoryDocument>(
    `${API_ENDPOINTS.INVENTORY.DOCUMENTS(orgId, warehouseId)}/${documentId}/cancel`
  );
  return response.data;
};

export const claimOrderStockMove = async (
  orgId: string,
  warehouseId: string,
  orderId: string
): Promise<InventoryDocument> => {
  const response = await apiClient.post<InventoryDocument>(
    `/organizations/${orgId}/warehouses/${warehouseId}/orders/${orderId}/claim`
  );
  return response.data;
};

// ─── REPLENISHMENTS ──────────────────────────────────────────────────────────

export const getReplenishmentRequests = async (
  orgId: string,
  warehouseId: string,
  params?: { page?: number; limit?: number }
): Promise<PaginatedResponse<ReplenishmentRequest>> => {
  const response = await apiClient.get<PaginatedResponse<ReplenishmentRequest>>(
    API_ENDPOINTS.INVENTORY.REPLENISHMENT_REQUESTS(orgId, warehouseId),
    { params }
  );
  return response.data;
};

export const createReplenishmentRequest = async (
  orgId: string,
  warehouseId: string,
  inventoryDocumentId: string,
  data: CreateReplenishmentRequest
): Promise<ReplenishmentRequest> => {
  const response = await apiClient.post<ReplenishmentRequest>(
    API_ENDPOINTS.INVENTORY.REPLENISHMENT_REQUESTS(orgId, warehouseId),
    {
      inventoryDocumentId,
      ...data,
    }
  );
  return response.data;
};

// ─── COGS & VALUATION ────────────────────────────────────────────────────────

export const getOrderCOGS = async (
  orgId: string,
  orderId: string
): Promise<StockValuation[]> => {
  const response = await apiClient.get<StockValuation[]>(
    API_ENDPOINTS.INVENTORY.COGS(orgId, orderId)
  );
  return response.data;
};

// ─── INVENTORY ANALYTICS ─────────────────────────────────────────────────────

export interface StockValuationTrendPoint {
  date: string;
  valuation: number;
  inboundValue: number;
  outboundValue: number;
  netChange: number;
}

export interface AssetCategoryDistribution {
  categoryId: string;
  categoryName: string;
  totalAssetValue: number;
  percentage: number;
  totalQuantity: number;
  productCount: number;
}

export const getStockValuationTrend = async (
  orgId: string,
  params?: { months?: number; year?: number }
): Promise<StockValuationTrendPoint[]> => {
  const response = await apiClient.get<StockValuationTrendPoint[]>(
    API_ENDPOINTS.ANALYTICS.INVENTORY_VALUATION_TREND(orgId),
    { params }
  );
  return response.data;
};

export const getAssetCategoryDistribution = async (
  orgId: string
): Promise<AssetCategoryDistribution[]> => {
  const response = await apiClient.get<AssetCategoryDistribution[]>(
    API_ENDPOINTS.ANALYTICS.INVENTORY_ASSET_DISTRIBUTION(orgId)
  );
  return response.data;
};
