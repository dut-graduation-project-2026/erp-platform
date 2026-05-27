package com.dut.erp.service;

import com.dut.erp.dto.request.CreateSaleOrderRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleOrderBaseResponse;
import com.dut.erp.dto.response.SaleOrderResponse;
import java.util.UUID;

public interface SaleOrderService {

  PagedEntityResponse<SaleOrderBaseResponse> getOrdersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  SaleOrderResponse getOrderById(UUID organizationId, UUID orderId);

  SaleOrderResponse createOrder(UUID organizationId, CreateSaleOrderRequest request);

  SaleOrderResponse updateOrder(UUID organizationId, UUID orderId, CreateSaleOrderRequest request);
}
