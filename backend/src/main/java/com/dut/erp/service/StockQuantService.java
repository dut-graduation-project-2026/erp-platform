package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockQuantResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface StockQuantService {

  PagedEntityResponse<StockQuantResponse> getStockQuants(
      UUID organizationId,
      UUID productId,
      UUID warehouseId,
      UUID locationId,
      PaginationRequest paginationRequest
  );

  void addStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity);

  void reserveStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity);

  void releaseReservedStock(UUID organizationId, UUID productId, UUID locationId, UUID lotId, BigDecimal quantity);
}
