package com.dut.erp.service;

import com.dut.erp.dto.request.CreateWarehouseRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateWarehouseRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.WarehouseBaseResponse;
import com.dut.erp.dto.response.WarehouseResponse;
import com.dut.erp.dto.response.WarehouseMetricsResponse;
import java.util.UUID;

public interface WarehouseService {

  PagedEntityResponse<WarehouseBaseResponse> getWarehouses(
      UUID organizationId, PaginationRequest paginationRequest);

  WarehouseResponse getWarehouseById(UUID organizationId, UUID warehouseId);

  WarehouseResponse createWarehouse(UUID organizationId, CreateWarehouseRequest request);

  WarehouseResponse updateWarehouse(
      UUID organizationId, UUID warehouseId, UpdateWarehouseRequest request);

  void deleteWarehouse(UUID organizationId, UUID warehouseId);

  WarehouseMetricsResponse getWarehouseMetrics(UUID organizationId, UUID warehouseId);
}
