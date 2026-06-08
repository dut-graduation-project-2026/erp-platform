package com.dut.erp.service;

import com.dut.erp.dto.request.UpsertOrderItemRequest;
import com.dut.erp.dto.response.OrderItemResponse;
import java.util.List;
import java.util.UUID;

public interface OrderItemService {

  List<OrderItemResponse> getOrderItems(UUID organizationId, UUID orderId);

  OrderItemResponse getOrderItemById(UUID organizationId, UUID orderId, UUID id);

  OrderItemResponse createOrderItem(UUID organizationId, UUID orderId, UpsertOrderItemRequest request);

  OrderItemResponse updateOrderItem(UUID organizationId, UUID orderId, UUID id, UpsertOrderItemRequest request);

  void deleteOrderItem(UUID organizationId, UUID orderId, UUID id);
}
