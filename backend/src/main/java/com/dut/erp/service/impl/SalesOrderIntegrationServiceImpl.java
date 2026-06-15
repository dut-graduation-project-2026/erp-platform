package com.dut.erp.service.impl;

import com.dut.erp.entity.Order;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.service.InventoryDocumentService;
import com.dut.erp.service.SalesOrderIntegrationService;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.WarehouseRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderIntegrationServiceImpl implements SalesOrderIntegrationService {

  private final InventoryDocumentService inventoryDocumentService;
  private final OrderRepository orderRepository;
  private final WarehouseRepository warehouseRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;

  @Override
  @Transactional
  public void handleOrderConfirmation(Order order, UUID warehouseId) {
    if (warehouseId == null) {
      log.info("SalesOrderIntegration: Order {} confirmed without warehouse. Pending fulfillment.", order.getId());
      return;
    }

    log.info("SalesOrderIntegration: Automatically creating warehouse issue document for order {} (number: {}) in warehouse {}", 
        order.getId(), order.getOrderNumber(), warehouseId);

    inventoryDocumentService.createIssueDocumentFromOrder(
        order.getOrganization().getId(), 
        warehouseId, 
        order.getId()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<com.dut.erp.dto.response.RouteProposalResponse> previewSmartRoute(UUID organizationId) {
    log.info("Previewing smart routing for organization: {}", organizationId);
    List<com.dut.erp.dto.response.RouteProposalResponse> proposals = new java.util.ArrayList<>();

    // 1. Get all CONFIRMED orders
    List<Order> pendingOrders = orderRepository.findByOrganizationIdAndStatus(organizationId, com.dut.erp.enums.OrderStatus.CONFIRMED);
    if (pendingOrders.isEmpty()) {
      return proposals;
    }

    // 2. Get all warehouses for the organization
    List<Warehouse> warehouses = warehouseRepository.findAllByOrganizationId(organizationId);

    // 3. For each order, try to find a warehouse with sufficient stock
    for (Order order : pendingOrders) {
      Warehouse proposedWarehouse = null;
      boolean found = false;

      if (!warehouses.isEmpty()) {
        for (Warehouse warehouse : warehouses) {
          boolean hasSufficientStock = true;

          for (com.dut.erp.entity.OrderItem item : order.getItems()) {
            java.util.Optional<com.dut.erp.entity.InventoryBalance> balanceOpt = 
                inventoryBalanceRepository.findByWarehouseIdAndProductId(warehouse.getId(), item.getProduct().getId());
            
            if (balanceOpt.isEmpty() || balanceOpt.get().getQuantity().compareTo(item.getQuantity()) < 0) {
              hasSufficientStock = false;
              break;
            }
          }

          if (hasSufficientStock) {
            proposedWarehouse = warehouse;
            found = true;
            break; // Stop looking for warehouses for this order
          }
        }
      }

      proposals.add(new com.dut.erp.dto.response.RouteProposalResponse(
          order.getId(),
          order.getOrderNumber(),
          order.getPartner() != null ? order.getPartner().getName() : "Unknown Customer",
          order.getTotalAmount(),
          found ? proposedWarehouse.getId() : null,
          found ? proposedWarehouse.getName() : null,
          found
      ));
    }

    return proposals;
  }

  @Override
  @Transactional
  public void confirmSmartRoute(UUID organizationId, com.dut.erp.dto.request.ConfirmRouteRequest request) {
    log.info("Confirming smart routing for organization: {} with {} confirmations", organizationId, request.routeConfirmations().size());

    for (com.dut.erp.dto.request.ConfirmRouteRequest.RouteConfirmation confirmation : request.routeConfirmations()) {
      Order order = orderRepository.findById(confirmation.orderId())
          .orElseThrow(() -> new com.dut.erp.exception.ResourceNotFoundException("Order not found with id: " + confirmation.orderId()));
      
      if (!order.getOrganization().getId().equals(organizationId)) {
        throw new BadRequestException("Order " + order.getOrderNumber() + " does not belong to your organization.");
      }

      if (order.getStatus() != com.dut.erp.enums.OrderStatus.CONFIRMED) {
        throw new BadRequestException("Order " + order.getOrderNumber() + " is not in CONFIRMED status.");
      }

      log.info("Routing confirmed order {} to warehouse {}", order.getOrderNumber(), confirmation.warehouseId());
      inventoryDocumentService.createIssueDocumentFromOrder(organizationId, confirmation.warehouseId(), order.getId());
    }
  }
}
