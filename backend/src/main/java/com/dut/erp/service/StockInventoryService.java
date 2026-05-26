package com.dut.erp.service;

import com.dut.erp.dto.request.CreateStockInventoryRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.SubmitInventoryCountRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockInventoryBaseResponse;
import com.dut.erp.dto.response.StockInventoryResponse;
import java.util.UUID;

public interface StockInventoryService {

  PagedEntityResponse<StockInventoryBaseResponse> getInventories(
      UUID organizationId, PaginationRequest paginationRequest);

  StockInventoryResponse getInventoryById(UUID organizationId, UUID inventoryId);

  StockInventoryResponse createInventory(UUID organizationId, CreateStockInventoryRequest request);

  StockInventoryResponse startInventory(UUID organizationId, UUID inventoryId);

  StockInventoryResponse submitCounts(UUID organizationId, UUID inventoryId, SubmitInventoryCountRequest request);

  StockInventoryResponse validateInventory(UUID organizationId, UUID inventoryId);
}
