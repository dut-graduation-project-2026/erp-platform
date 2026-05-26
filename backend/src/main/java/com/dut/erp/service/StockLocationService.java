package com.dut.erp.service;

import com.dut.erp.dto.request.CreateStockLocationRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateStockLocationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockLocationBaseResponse;
import com.dut.erp.dto.response.StockLocationResponse;
import java.util.UUID;

public interface StockLocationService {

  PagedEntityResponse<StockLocationBaseResponse> getLocationsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  PagedEntityResponse<StockLocationBaseResponse> getLocationsByWarehouseId(
      UUID organizationId, UUID warehouseId, PaginationRequest paginationRequest);

  StockLocationResponse getLocationById(UUID organizationId, UUID locationId);

  StockLocationResponse createLocation(UUID organizationId, CreateStockLocationRequest request);

  StockLocationResponse updateLocation(
      UUID organizationId, UUID locationId, UpdateStockLocationRequest request);

  void deleteLocation(UUID organizationId, UUID locationId);
}
