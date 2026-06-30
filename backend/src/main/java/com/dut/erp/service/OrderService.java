package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateOrderStatusRequest;
import com.dut.erp.dto.request.UpsertOrderRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.enums.OrderStatus;
import java.time.Instant;
import java.util.UUID;

public interface OrderService {

  PagedEntityResponse<OrderBaseResponse> getQuotationsWithFilterByOrganizationId(
      UUID organizationId, String search, UUID saleTeamId, PaginationRequest paginationRequest);

  PagedEntityResponse<OrderBaseResponse> getOrdersWithFilterByOrganizationId(
      UUID organizationId,
      String search,
      OrderStatus status,
      UUID partnerId,
      UUID salePersonId,
      UUID saleTeamId,
      Instant startDate,
      Instant endDate,
      PaginationRequest paginationRequest);

  OrderResponse getQuotationById(UUID organizationId, UUID id);

  OrderResponse getOrderById(UUID organizationId, UUID id);

  OrderResponse createQuotation(UUID organizationId, UpsertOrderRequest request);

  OrderResponse updateQuotation(UUID organizationId, UUID id, UpsertOrderRequest request);

  OrderResponse updateOrderStatus(UUID organizationId, UUID id, UpdateOrderStatusRequest request);

  PagedEntityResponse<OrderBaseResponse> getOrdersByStatus(
      UUID organizationId, com.dut.erp.enums.OrderStatus status, PaginationRequest paginationRequest);

  void deleteQuotation(UUID organizationId, UUID id);
}
