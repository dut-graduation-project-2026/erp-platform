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
  RouteProposalResponse,
  ConfirmRouteRequest,
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

// ─── INVENTORY METRICS ───────────────────────────────────────────────────────

export interface MetricDetail {
  toProcess: number;
  backorders: number;
  late: number;
}

export interface WarehouseMetricsResponse {
  receipts: MetricDetail;
  deliveries: MetricDetail;
  internalTransfers: MetricDetail;
  pendingFulfillmentCount: number;
}

export const getWarehouseMetrics = async (
  orgId: string,
  warehouseId: string
): Promise<WarehouseMetricsResponse> => {
  const response = await apiClient.get<WarehouseMetricsResponse>(
    API_ENDPOINTS.INVENTORY.METRICS(orgId, warehouseId)
  );
  return response.data;
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

export const previewSmartRoute = async (
  orgId: string,
  warehouseId?: string
): Promise<RouteProposalResponse[]> => {
  const response = await apiClient.get<RouteProposalResponse[]>(
    `/organizations/${orgId}/orders/smart-route/preview`,
    { params: { warehouseId } }
  );
  return response.data;
};

export const confirmSmartRoute = async (
  orgId: string,
  data: ConfirmRouteRequest
): Promise<void> => {
  await apiClient.post<void>(
    `/organizations/${orgId}/orders/smart-route/confirm`,
    data
  );
};

// ─── REPLENISHMENTS ──────────────────────────────────────────────────────────

export const getReplenishmentRequests = async (
  orgId: string,
  warehouseId: string,
  params?: { search?: string; page?: number; limit?: number }
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

// ─────────────────────────────────────────────────────────────────────────────
// ACTIONABLE AI: INVENTORY INTELLIGENCE & REORDERING
// ─────────────────────────────────────────────────────────────────────────────

export interface ProductAbcXyz {
  productId: string;
  productName: string;
  abcClass: string;
  xyzClass: string;
  currentStock: number;
  rop: number;
  eoq: number;
  status: 'OK' | 'WARNING' | 'CRITICAL';
}

export interface AiInventoryAnalysisResponse {
  summary: string;
  abc_xyz_matrix: ProductAbcXyz[];
  critical_stock_count: number;
  recommendations: string[];
}

export interface AiReorderItem {
  productId: string;
  productName: string;
  warehouseId: string;
  warehouseName: string;
  currentStock: number;
  rop: number;
  eoq: number;
  recommendedQuantity: number;
  urgency: 'HIGH' | 'MEDIUM' | 'LOW';
  notes: string;
}

export interface AiReorderRecommendationResponse {
  recommendations: AiReorderItem[];
}

export const getAiInventoryAnalysis = async (
  orgId: string,
  forceRefresh: boolean = false
): Promise<AiInventoryAnalysisResponse> => {
  const response = await apiClient.get<AiInventoryAnalysisResponse>(
    `/organizations/${orgId}/ai/inventory/analysis`,
    { params: { forceRefresh } }
  );
  return response.data;
};

export const getAiInventoryAlerts = async (
  orgId: string
): Promise<string[]> => {
  const response = await apiClient.get<string[]>(
    `/organizations/${orgId}/ai/inventory/alerts`
  );
  return response.data;
};

export const getAiReorderRecommendations = async (
  orgId: string
): Promise<AiReorderRecommendationResponse> => {
  const response = await apiClient.get<AiReorderRecommendationResponse>(
    `/organizations/${orgId}/ai/reorder/recommendations`
  );
  return response.data;
};

export const confirmAiReorders = async (
  orgId: string,
  warehouseId: string,
  recommendations: Partial<AiReorderItem>[]
): Promise<void> => {
  await apiClient.post(
    `/organizations/${orgId}/ai/reorder/confirm`,
    recommendations,
    { params: { warehouseId } }
  );
};

