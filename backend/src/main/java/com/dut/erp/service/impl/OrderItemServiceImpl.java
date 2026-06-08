package com.dut.erp.service.impl;

import com.dut.erp.dto.request.UpsertOrderItemRequest;
import com.dut.erp.dto.response.OrderItemResponse;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.OrderItem;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.Tax;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrderItemMapper;
import com.dut.erp.repository.OrderItemRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.TaxRepository;
import com.dut.erp.service.OrderItemService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final TaxRepository taxRepository;
  private final OrderItemMapper orderItemMapper;

  @Override
  public List<OrderItemResponse> getOrderItems(UUID organizationId, UUID orderId) {
    log.info("Fetching items for order {} in organization {}", orderId, organizationId);
    findOrderByIdAndOrganizationId(orderId, organizationId); // Validate order exists
    return orderItemRepository.findAllByOrderIdAndOrganizationId(orderId, organizationId).stream()
        .map(orderItemMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  public OrderItemResponse getOrderItemById(UUID organizationId, UUID orderId, UUID id) {
    log.info("Fetching item {} for order {} in organization {}", id, orderId, organizationId);
    OrderItem orderItem = findOrderItemByIdAndOrderIdAndOrganizationId(id, orderId, organizationId);
    return orderItemMapper.toResponse(orderItem);
  }

  @Override
  @Transactional
  public OrderItemResponse createOrderItem(
      UUID organizationId, UUID orderId, UpsertOrderItemRequest request) {
    log.info("Creating item for order {} in organization {}", orderId, organizationId);
    Order order = findOrderByIdAndOrganizationId(orderId, organizationId);
    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Order items can only be modified for draft orders");
    }
    Product product = findProductByIdAndOrganizationId(request.productId(), organizationId);
    Tax tax =
        request.taxId() != null
            ? findTaxByIdAndOrganizationId(request.taxId(), organizationId)
            : null;

    BigDecimal subtotal =
        request.quantity().multiply(request.unitPrice()).setScale(2, RoundingMode.HALF_UP);

    OrderItem orderItem =
        OrderItem.builder()
            .organization(order.getOrganization())
            .order(order)
            .product(product)
            .tax(tax)
            .quantity(request.quantity())
            .unitPrice(request.unitPrice())
            .subtotal(subtotal)
            .build();

    orderItem = orderItemRepository.save(orderItem);
    recalculateAndSaveOrderTotal(order);

    return orderItemMapper.toResponse(orderItem);
  }

  @Override
  @Transactional
  public OrderItemResponse updateOrderItem(
      UUID organizationId, UUID orderId, UUID id, UpsertOrderItemRequest request) {
    log.info("Updating item {} for order {} in organization {}", id, orderId, organizationId);
    Order order = findOrderByIdAndOrganizationId(orderId, organizationId);
    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Order items can only be modified for draft orders");
    }
    OrderItem orderItem = findOrderItemByIdAndOrderIdAndOrganizationId(id, orderId, organizationId);
    Product product = findProductByIdAndOrganizationId(request.productId(), organizationId);
    Tax tax =
        request.taxId() != null
            ? findTaxByIdAndOrganizationId(request.taxId(), organizationId)
            : null;

    BigDecimal subtotal =
        request.quantity().multiply(request.unitPrice()).setScale(2, RoundingMode.HALF_UP);

    orderItem.setProduct(product);
    orderItem.setTax(tax);
    orderItem.setQuantity(request.quantity());
    orderItem.setUnitPrice(request.unitPrice());
    orderItem.setSubtotal(subtotal);

    orderItem = orderItemRepository.save(orderItem);

    // Recalculate order total
    recalculateAndSaveOrderTotal(order);

    return orderItemMapper.toResponse(orderItem);
  }

  @Override
  @Transactional
  public void deleteOrderItem(UUID organizationId, UUID orderId, UUID id) {
    log.info("Deleting item {} from order {} in organization {}", id, orderId, organizationId);
    Order order = findOrderByIdAndOrganizationId(orderId, organizationId);
    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Order items can only be modified for draft orders");
    }
    OrderItem orderItem = findOrderItemByIdAndOrderIdAndOrganizationId(id, orderId, organizationId);

    orderItemRepository.delete(orderItem);
    recalculateAndSaveOrderTotal(order);
  }

  // ---- Private helpers ----

  private void recalculateAndSaveOrderTotal(Order order) {
    BigDecimal total =
        orderItemRepository.sumSubtotalByOrderId(order.getId()).setScale(2, RoundingMode.HALF_UP);
    order.setTotalAmount(total);
    orderRepository.save(order);
  }

  private Order findOrderByIdAndOrganizationId(UUID orderId, UUID organizationId) {
    return orderRepository
        .findShallowByIdAndOrganizationId(orderId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
  }

  private OrderItem findOrderItemByIdAndOrderIdAndOrganizationId(
      UUID id, UUID orderId, UUID organizationId) {
    return orderItemRepository
        .findByIdAndOrderIdAndOrganizationId(id, orderId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + id));
  }

  private Product findProductByIdAndOrganizationId(UUID productId, UUID organizationId) {
    return productRepository
        .findByIdAndOrganizationId(productId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Product not found with id: " + productId));
  }

  private Tax findTaxByIdAndOrganizationId(UUID taxId, UUID organizationId) {
    return taxRepository
        .findByIdAndOrganizationId(taxId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Tax not found with id: " + taxId));
  }
}
