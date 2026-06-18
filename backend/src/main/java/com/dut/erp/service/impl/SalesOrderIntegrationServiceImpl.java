package com.dut.erp.service.impl;

import com.dut.erp.entity.Order;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.service.InventoryDocumentService;
import com.dut.erp.service.SalesOrderIntegrationService;
import com.dut.erp.service.GeocodingService;
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

  private final GeocodingService geocodingService;

  @Override
  @Transactional(readOnly = true)
  public List<com.dut.erp.dto.response.RouteProposalResponse> previewSmartRoute(UUID organizationId, UUID warehouseId) {
    log.info("Previewing smart routing for organization: {} and warehouse: {}", organizationId, warehouseId);
    List<com.dut.erp.dto.response.RouteProposalResponse> proposals = new java.util.ArrayList<>();

    // 1. Get all CONFIRMED orders
    List<Order> pendingOrders = orderRepository.findByOrganizationIdAndStatus(organizationId, com.dut.erp.enums.OrderStatus.CONFIRMED);
    if (pendingOrders.isEmpty()) {
      return proposals;
    }

    // 2. Get warehouses for the organization (filtered by warehouseId if provided)
    List<Warehouse> warehouses;
    if (warehouseId != null) {
      warehouses = warehouseRepository.findById(warehouseId)
          .map(List::of)
          .orElse(List.of());
    } else {
      warehouses = warehouseRepository.findAllByOrganizationId(organizationId);
    }

    // 3. For each order, try to find the closest warehouse with sufficient stock
    for (Order order : pendingOrders) {
      Warehouse proposedWarehouse = null;
      boolean found = false;
      double minDistance = Double.MAX_VALUE;

      // Geocode the destination address (customer's address)
      String orderAddress = order.getPartner() != null ? order.getPartner().getAddress() : null;
      Double orderLat = null;
      Double orderLng = null;

      if (orderAddress != null && !orderAddress.isBlank()) {
        List<com.dut.erp.dto.response.GeocodingSearchResult> orderCoords = geocodingService.search(orderAddress, null);
        if (orderCoords != null && !orderCoords.isEmpty() && orderCoords.get(0).lat() != null && orderCoords.get(0).lng() != null) {
          orderLat = orderCoords.get(0).lat();
          orderLng = orderCoords.get(0).lng();
        }
      }

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
            double distance = Double.MAX_VALUE;
            String whAddress = warehouse.getAddress();

            // Geocode the warehouse address and calculate distance
            if (orderLat != null && orderLng != null && whAddress != null && !whAddress.isBlank()) {
              List<com.dut.erp.dto.response.GeocodingSearchResult> whCoords = geocodingService.search(whAddress, null);
              if (whCoords != null && !whCoords.isEmpty() && whCoords.get(0).lat() != null && whCoords.get(0).lng() != null) {
                distance = calculateDistance(orderLat, orderLng, whCoords.get(0).lat(), whCoords.get(0).lng());
              }
            }

            // Select the warehouse with sufficient stock that is closest to the order address
            if (proposedWarehouse == null || distance < minDistance) {
              proposedWarehouse = warehouse;
              minDistance = distance;
              found = true;
            }
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

  /** Calculate geographic distance using Haversine formula (in km) */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // Earth radius in km
    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
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
