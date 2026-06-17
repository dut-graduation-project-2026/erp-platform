import { Organization } from '@/types/organization';
import { 
  DocumentType, 
  ReferenceType, 
  DocumentStatus, 
  CogsMethod 
} from '@/config/constants';

// ─── ENUMS ───────────────────────────────────────────────────────────────────

export type { DocumentType, ReferenceType, DocumentStatus, CogsMethod };

// ─── USER & WAREHOUSE BASIC METADATA ─────────────────────────────────────────

export interface UserBaseResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  avatarUrl?: string;
}

export interface WarehouseBaseResponse {
  id: string;
  code: string;
  name: string;
}

export interface ProductBaseResponse {
  id: string;
  name: string;
  sku: string;
  price: number;
}

// ─── REQUEST INTERFACES ──────────────────────────────────────────────────────

export interface CreateWarehouseRequest {
  name: string;
  code: string;
  address: string;
  description?: string;
  managerId: string;
  staffIds: string[];
}

export interface InventoryDocumentItemRequest {
  productId: string;
  quantity: number;
}

export interface CreateInventoryDocumentRequest {
  documentType: DocumentType;
  transferSourceWarehouseId?: string; // Used if TRANSFER_IN/TRANSFER_OUT or ADJUSTMENT
  scheduledDate: string; // ISO String
  notes?: string;
  items: InventoryDocumentItemRequest[];
}

export interface CreateReplenishmentRequest {
  notes?: string;
}

// ─── RESPONSE INTERFACES ─────────────────────────────────────────────────────

export interface Warehouse {
  id: string;
  code: string;
  name: string;
  address: string;
  description?: string;
  isActive: boolean;
  organization?: Organization;
  manager?: UserBaseResponse;
  staff?: UserBaseResponse[];
}

export interface InventoryBalance {
  id: string;
  warehouse: WarehouseBaseResponse;
  product: ProductBaseResponse;
  quantity: number;
  updatedAt: string;
  updatedBy?: UserBaseResponse;
}

export interface InventoryDocumentLine {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitCost: number;
  valuation: number;
}

export interface InventoryDocument {
  id: string;
  warehouseId: string;
  warehouseName: string;
  sourceWarehouseId?: string;
  sourceWarehouseName?: string;
  name: string;
  documentType: DocumentType;
  referenceType: ReferenceType;
  referenceId?: string;
  orderNumber?: string;
  partnerName?: string;
  deliveryAddress?: string;
  documentStatus: DocumentStatus;
  notes?: string;
  scheduledDate: string;
  dateDone?: string;
  lines: InventoryDocumentLine[];
  createdAt: string;
  updatedAt: string;
  createdBy?: UserBaseResponse;
  updatedBy?: UserBaseResponse;
  hasActiveReplenishment?: boolean;
}

export interface StockValuation {
  id: string;
  inventoryDocumentLineId: string;
  productId: string;
  productName: string;
  quantity: number;
  unitCost: number;
  totalValuation: number;
  method: CogsMethod;
  createdAt: string;
}

export interface ReplenishmentRequest {
  id: string;
  warehouseId: string;
  warehouseName: string;
  inventoryDocumentId: string;
  inventoryDocumentName: string;
  requestedBy: UserBaseResponse;
  notes?: string;
  status: 'OPEN' | 'RESOLVED';
  createdAt: string;
  orderNumber?: string;
  referenceId?: string;
}

export interface RouteProposalResponse {
  orderId: string;
  orderNumber: string;
  customerName: string;
  totalAmount: number;
  proposedWarehouseId?: string;
  proposedWarehouseName?: string;
  routable: boolean;
}

export interface ConfirmRouteRequest {
  routeConfirmations: {
    orderId: string;
    warehouseId: string;
  }[];
}
