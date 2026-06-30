package com.dut.erp.service;

import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryDocumentBaseResponse;
import com.dut.erp.dto.response.InventoryDocumentResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface InventoryDocumentService {

  InventoryDocumentResponse createDocument(
      UUID organizationId, UUID warehouseId, CreateInventoryDocumentRequest request);

  InventoryDocumentResponse createIssueDocumentFromOrder(UUID organizationId, UUID warehouseId, UUID orderId);

  PagedEntityResponse<InventoryDocumentBaseResponse> getDocuments(
      UUID organizationId, UUID warehouseId, String search, String status, String type, PaginationRequest paginationRequest);

  InventoryDocumentResponse getDocumentById(UUID organizationId, UUID warehouseId, UUID documentId);

  InventoryDocumentResponse confirmDocument(UUID organizationId, UUID warehouseId, UUID documentId);

  InventoryDocumentResponse completeDocument(UUID organizationId, UUID warehouseId, UUID documentId);

  InventoryDocumentResponse sentDocument(UUID organizationId, UUID warehouseId, UUID documentId);

  InventoryDocumentResponse cancelDocument(UUID organizationId, UUID warehouseId, UUID documentId);
}
