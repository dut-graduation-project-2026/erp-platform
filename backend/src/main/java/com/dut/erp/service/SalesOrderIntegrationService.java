package com.dut.erp.service;

import com.dut.erp.dto.request.ConfirmRouteRequest;
import com.dut.erp.dto.response.RouteProposalResponse;
import com.dut.erp.entity.Order;
import java.util.List;
import java.util.UUID;

public interface SalesOrderIntegrationService {
  void handleOrderConfirmation(Order order, UUID warehouseId);
  List<RouteProposalResponse> previewSmartRoute(UUID organizationId, UUID warehouseId);
  void confirmSmartRoute(UUID organizationId, ConfirmRouteRequest request);
}
