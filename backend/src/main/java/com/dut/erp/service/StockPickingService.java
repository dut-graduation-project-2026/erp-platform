package com.dut.erp.service;

import com.dut.erp.dto.request.CreateStockPickingRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockPickingBaseResponse;
import com.dut.erp.dto.response.StockPickingResponse;
import com.dut.erp.enums.PickingType;
import java.util.UUID;

public interface StockPickingService {

  PagedEntityResponse<StockPickingBaseResponse> getPickings(
      UUID organizationId, PickingType pickingType, PaginationRequest paginationRequest);

  StockPickingResponse getPickingById(UUID organizationId, UUID pickingId);

  StockPickingResponse createPicking(UUID organizationId, CreateStockPickingRequest request);

  StockPickingResponse confirmPicking(UUID organizationId, UUID pickingId);

  StockPickingResponse assignPicking(UUID organizationId, UUID pickingId);

  StockPickingResponse completePicking(UUID organizationId, UUID pickingId);
}
