package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryBalanceBaseResponse;
import com.dut.erp.dto.response.InventoryBalanceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockLayerResponse;
import java.util.List;
import java.util.UUID;

public interface InventoryBalanceService {

  /**
   * Returns a paginated list of inventory balances for the given warehouse.
   * Optionally filters by product name.
   */
  PagedEntityResponse<InventoryBalanceBaseResponse> getBalancesByWarehouse(
      UUID organizationId,
      UUID warehouseId,
      String search,
      PaginationRequest paginationRequest);

  /**
   * Returns the detail of a single inventory balance record.
   */
  InventoryBalanceResponse getBalanceById(UUID organizationId, UUID warehouseId, UUID balanceId);

  /**
   * Returns active stock layers (virtual lots) for a product in a warehouse.
   */
  List<StockLayerResponse> getActiveLayers(UUID organizationId, UUID warehouseId, UUID productId);
}
