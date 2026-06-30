package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.event.OrderStatusChangedEvent;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateOrderStatusRequest;
import com.dut.erp.dto.request.UpsertOrderRequest;
import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.InventoryDocumentLine;
import com.dut.erp.entity.Invoice;
import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.enums.ReferenceType;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrderMapper;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.InvoiceRepository;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.SaleTeamRepository;
import com.dut.erp.service.OrderService;
import com.dut.erp.service.SalesOrderIntegrationService;
import com.dut.erp.service.SecurityAuthService;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.util.SecurityUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.ReplenishmentRequestRepository;
import com.dut.erp.enums.ReplenishmentStatus;
import com.dut.erp.dto.event.ReplenishmentRequestStatusChangedEvent;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

  private final OrganizationRepository organizationRepository;
  private final OrderRepository orderRepository;
  private final LeadRepository leadRepository;
  private final SaleTeamRepository saleTeamRepository;
  private final OrderMapper orderMapper;
  private final InvoiceRepository invoiceRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;
  private final SalesOrderIntegrationService salesOrderIntegrationService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SecurityAuthService securityAuthService;
  private final PermissionRepository permissionRepository;

  @Override
  public PagedEntityResponse<OrderBaseResponse> getQuotationsWithFilterByOrganizationId(
      UUID organizationId, String search, UUID saleTeamId, PaginationRequest paginationRequest) {
    log.info("Fetching quotations for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("updatedAt")));

    CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
    boolean isAllOrders = securityAuthService.isAdmin(currentUser) || 
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            currentUser.getId(), organizationId, "orders:read_all"
        );

    Page<UUID> ids;
    if (isAllOrders) {
      ids = orderRepository.findQuotationIdsWithFilters(
          organizationId,
          (search != null && !search.trim().isEmpty()) ? search : null,
          saleTeamId,
          pageable);
    } else {
      List<UUID> teamIds = saleTeamRepository.findIdsByOrganizationIdAndUserId(organizationId, currentUser.getId());
      if (teamIds.isEmpty()) {
        teamIds = List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
      }
      ids = orderRepository.findQuotationIdsWithFiltersAndUser(
          organizationId,
          (search != null && !search.trim().isEmpty()) ? search : null,
          saleTeamId,
          currentUser.getId(),
          teamIds,
          pageable);
    }

    return getPagedResponseFromIds(ids, pageable);
  }

  @Override
  public PagedEntityResponse<OrderBaseResponse> getOrdersWithFilterByOrganizationId(
      UUID organizationId,
      String search,
      OrderStatus status,
      UUID partnerId,
      UUID salePersonId,
      UUID saleTeamId,
      Instant startDate,
      Instant endDate,
      PaginationRequest paginationRequest) {
    log.info("Fetching orders for organization {} with filters", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("updatedAt")));

    CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
    boolean isAllOrders = securityAuthService.isAdmin(currentUser) || 
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            currentUser.getId(), organizationId, "orders:read_all"
        );

    Page<UUID> ids;
    if (isAllOrders) {
      ids = orderRepository.findOrderIdsWithFilters(
              organizationId,
              (search != null && !search.trim().isEmpty()) ? search : null,
              status,
              partnerId,
              salePersonId,
              saleTeamId,
              startDate,
              endDate,
              pageable);
    } else {
      List<UUID> teamIds = saleTeamRepository.findIdsByOrganizationIdAndUserId(organizationId, currentUser.getId());
      if (teamIds.isEmpty()) {
        teamIds = List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
      }
      ids = orderRepository.findOrderIdsWithFiltersAndUser(
              organizationId,
              (search != null && !search.trim().isEmpty()) ? search : null,
              status,
              partnerId,
              salePersonId,
              saleTeamId,
              startDate,
              endDate,
              currentUser.getId(),
              teamIds,
              pageable);
    }

    return getPagedResponseFromIds(ids, pageable);
  }

  @Override
  public PagedEntityResponse<OrderBaseResponse> getOrdersByStatus(
      UUID organizationId, OrderStatus status, PaginationRequest paginationRequest) {
    log.info("Fetching orders with status {} for organization {}", status, organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("updatedAt")));

    CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
    boolean isAllOrders = securityAuthService.isAdmin(currentUser) || 
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            currentUser.getId(), organizationId, "orders:read_all"
        );

    Page<UUID> ids;
    if (isAllOrders) {
      ids = orderRepository.findIdsByOrganizationIdAndStatus(organizationId, status, pageable);
    } else {
      List<UUID> teamIds = saleTeamRepository.findIdsByOrganizationIdAndUserId(organizationId, currentUser.getId());
      if (teamIds.isEmpty()) {
        teamIds = List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
      }
      ids = orderRepository.findIdsByOrganizationIdAndStatusAndUser(organizationId, status, currentUser.getId(), teamIds, pageable);
    }

    return getPagedResponseFromIds(ids, pageable);
  }

  private PagedEntityResponse<OrderBaseResponse> getPagedResponseFromIds(
      Page<UUID> ids, Pageable pageable) {
    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Order> orderMap =
        orderRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Order::getId, Function.identity()));

    List<com.dut.erp.entity.InventoryDocument> activeDocs = inventoryDocumentRepository.findActiveDocumentsForOrders(
        ReferenceType.SALES_ORDER, ids.getContent(), DocumentType.ISSUE
    );
    Map<UUID, com.dut.erp.entity.InventoryDocument> docMap = activeDocs.stream()
        .collect(Collectors.toMap(
            com.dut.erp.entity.InventoryDocument::getReferenceId,
            Function.identity(),
            (d1, d2) -> d1
        ));

    List<OrderBaseResponse> responses =
        ids.getContent().stream()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .map(order -> {
              OrderBaseResponse base = orderMapper.toBaseResponse(order);
              com.dut.erp.entity.InventoryDocument doc = docMap.get(order.getId());
              if (doc != null) {
                return new OrderBaseResponse(
                    base.id(),
                    base.orderNumber(),
                    base.partner(),
                    base.status(),
                    base.totalAmount(),
                    base.createdAt(),
                    doc.getWarehouse().getId(),
                    doc.getWarehouse().getName(),
                    base.saleTeamId(),
                    base.saleTeamName()
                );
              }
              return base;
            })
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public OrderResponse getQuotationById(UUID organizationId, UUID id) {
    log.info("Fetching quotation {} for organization {}", id, organizationId);
    Order order = findOrderByIdAndOrganizationId(id, organizationId);
    securityAuthService.isOrderOwnerOrManagerOrAdmin(order, SecurityUtils.getCurrentUser());
    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Requested resource is an Order, not a Quotation");
    }
    return enrichWithWarehouse(orderMapper.toResponse(order));
  }

  @Override
  public OrderResponse getOrderById(UUID organizationId, UUID id) {
    log.info("Fetching order {} for organization {}", id, organizationId);
    Order order = findOrderByIdAndOrganizationId(id, organizationId);
    securityAuthService.isOrderOwnerOrManagerOrAdmin(order, SecurityUtils.getCurrentUser());
    if (order.getStatus() == OrderStatus.DRAFT) {
      throw new BadRequestException("Requested resource is a Quotation, not an Order");
    }
    return enrichWithWarehouse(orderMapper.toResponse(order));
  }

  @Override
  @Transactional
  public OrderResponse createQuotation(UUID organizationId, UpsertOrderRequest request) {
    Organization organization = findOrganizationById(organizationId);
    Lead lead = findLeadByIdAndOrganizationId(request.leadId(), organizationId);

    if (lead.getPartner() == null) {
      throw new BadRequestException("Lead must be promoted to a Customer (Partner) first");
    }

    Partner partner = lead.getPartner();
    String orderNumber = resolveOrderNumber(request.orderNumber(), organizationId);

    Order order =
        Order.builder()
            .organization(organization)
            .partner(partner)
            .lead(lead)
            .orderNumber(orderNumber)
            .deliveryDate(request.deliveryDate())
            .expirationDate(request.expirationDate())
            .status(OrderStatus.DRAFT)
            .totalAmount(BigDecimal.ZERO)
            .build();

    order = orderRepository.save(order);
    log.info("Created quotation {} in organization {}", order.getId(), organizationId);

    lead.setStage(LeadStage.PROPOSAL);
    leadRepository.save(lead);
    log.info("Updated lead {} stage to PROPOSAL since quotation was created", lead.getId());

    return enrichWithWarehouse(orderMapper.toResponse(order));
  }

  @Override
  @Transactional
  public OrderResponse updateQuotation(UUID organizationId, UUID id, UpsertOrderRequest request) {
    Order order = findOrderByIdAndOrganizationId(id, organizationId);

    securityAuthService.isOrderOwnerOrManagerOrAdmin(order, SecurityUtils.getCurrentUser());

    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Only quotations in DRAFT status can be updated");
    }

    Lead lead = findLeadByIdAndOrganizationId(request.leadId(), organizationId);

    if (lead.getPartner() == null) {
      throw new BadRequestException("Lead must be promoted to a Customer (Partner) first");
    }

    Partner partner = lead.getPartner();

    // If user provides a new number different from the current one, validate uniqueness.
    // If null/blank, keep the existing order number.
    String newOrderNumber =
        (request.orderNumber() != null && !request.orderNumber().isBlank())
            ? request.orderNumber()
            : order.getOrderNumber();

    if (!order.getOrderNumber().equals(newOrderNumber)
        && orderRepository.existsByOrganizationIdAndOrderNumber(organizationId, newOrderNumber)) {
      throw new BadRequestException("Order number already exists in this organization");
    }

    order.setPartner(partner);
    order.setLead(lead);
    order.setOrderNumber(newOrderNumber);
    order.setDeliveryDate(request.deliveryDate());
    order.setExpirationDate(request.expirationDate());

    order = orderRepository.save(order);
    log.info("Updated quotation {} in organization {}", id, organizationId);
    return enrichWithWarehouse(orderMapper.toResponse(order));
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(
      UUID organizationId, UUID id, UpdateOrderStatusRequest request) {
    Order order = findOrderWithLeadByIdAndOrganizationId(id, organizationId);

    securityAuthService.isOrderOwnerOrManagerOrAdmin(order, SecurityUtils.getCurrentUser());

    // Rule: Once status is COMPLETED, it cannot be updated
    if (order.getStatus() == OrderStatus.COMPLETED) {
      throw new BadRequestException("Cannot update status of a COMPLETED order");
    }

    // Rule: Sales module can only set status to DRAFT, CONFIRMED, CANCELLED, or COMPLETED
    if (request.status() != OrderStatus.DRAFT
        && request.status() != OrderStatus.CONFIRMED
        && request.status() != OrderStatus.CANCELLED
        && request.status() != OrderStatus.COMPLETED) {
      throw new BadRequestException(
          "Sales module is not allowed to manually update status to " + request.status());
    }

    // Rule: Transition to COMPLETED is only allowed if both delivery and payment are completed
    if (request.status() == OrderStatus.COMPLETED) {
      Invoice invoice =
          invoiceRepository
              .findByOrderIdAndOrganizationId(id, organizationId)
              .orElseThrow(
                  () ->
                      new BadRequestException(
                          "Cannot complete order because no invoice has been created for it yet"));

      if (invoice.getStatus() != InvoiceStatus.PAID) {
        throw new BadRequestException(
            "Cannot complete order because the linked invoice is not PAID");
      }

      boolean isDeliveryCompleted = inventoryDocumentRepository
          .findByReferenceTypeAndReferenceIdAndDocumentType(
              ReferenceType.SALES_ORDER, order.getId(), DocumentType.ISSUE)
          .map(doc -> doc.getDocumentStatus() == DocumentStatus.COMPLETED)
          .orElse(false);

      if (!isDeliveryCompleted) {
        throw new BadRequestException(
            "Cannot complete order because the linked warehouse delivery is not completed");
      }
    }

    // Rule: If current status is WAITING_FOR_STOCK, Sales team can only cancel it
    if (order.getStatus() == OrderStatus.WAITING_FOR_STOCK
        && request.status() != OrderStatus.CANCELLED) {
      throw new BadRequestException(
          "Cannot manually update order status in WAITING_FOR_STOCK state unless cancelling. Status"
              + " is managed by Warehouse.");
    }

    // Rule: If current status is SENT, Sales team can only cancel or complete it
    if (order.getStatus() == OrderStatus.SENT
        && request.status() != OrderStatus.COMPLETED
        && request.status() != OrderStatus.CANCELLED) {
      throw new BadRequestException(
          "Delivered orders (SENT status) can only be completed or cancelled");
    }

    // Rule: Once status changes away from DRAFT, it cannot transition back to DRAFT
    if (order.getStatus() != OrderStatus.DRAFT && request.status() == OrderStatus.DRAFT) {
      throw new BadRequestException("Cannot revert an Order back to a DRAFT Quotation");
    }

    // Rule: If order is CANCELLED, cancel the linked invoice and associated warehouse document
    if (request.status() == OrderStatus.CANCELLED) {
      invoiceRepository
          .findByOrderIdAndOrganizationId(id, organizationId)
          .ifPresent(
              invoice -> {
                invoice.setStatus(InvoiceStatus.CANCELLED);
                invoiceRepository.save(invoice);
                log.info(
                    "Automatically cancelled invoice {} because order {} was CANCELLED",
                    invoice.getId(),
                    id);
              });

      // Cancel warehouse document and revert stock if it was confirmed
      inventoryDocumentRepository
          .findByReferenceTypeAndReferenceIdAndDocumentType(
              ReferenceType.SALES_ORDER, id, DocumentType.ISSUE)
          .ifPresent(
              doc -> {
                if (doc.getDocumentStatus() != DocumentStatus.COMPLETED
                    && doc.getDocumentStatus() != DocumentStatus.CANCELLED) {

                  if (doc.getDocumentStatus() == DocumentStatus.CONFIRMED) {
                    // Revert stock moves
                    for (InventoryDocumentLine tx : doc.getLines()) {
                      InventoryBalance balance =
                          inventoryBalanceRepository
                              .findByWarehouseIdAndProductId(
                                  doc.getWarehouse().getId(), tx.getProduct().getId())
                              .orElseThrow(
                                  () ->
                                      new ResourceNotFoundException("Inventory balance not found"));
                      balance.setQuantity(balance.getQuantity().add(tx.getQuantity()));
                      inventoryBalanceRepository.save(balance);
                    }
                  }

                   doc.setDocumentStatus(DocumentStatus.CANCELLED);
                   inventoryDocumentRepository.save(doc);
                   log.info(
                       "Automatically cancelled inventory document {} because order {} was"
                           + " CANCELLED",
                       doc.getId(),
                       id);

                   // Automatically cancel associated replenishment request if exists
                   replenishmentRequestRepository.findByInventoryDocumentId(doc.getId())
                       .ifPresent(req -> {
                         if (req.getStatus() != ReplenishmentStatus.CANCELED) {
                           ReplenishmentStatus oldReqStatus = req.getStatus();
                           req.setStatus(ReplenishmentStatus.CANCELED);
                           replenishmentRequestRepository.save(req);
                           applicationEventPublisher.publishEvent(new ReplenishmentRequestStatusChangedEvent(
                               req.getId(), oldReqStatus, ReplenishmentStatus.CANCELED));
                           log.info(
                               "Automatically cancelled replenishment request {} because order {} was CANCELLED",
                               req.getId(),
                               id);
                         }
                       });
                 }
              });
    }

    OrderStatus oldStatus = order.getStatus();
    order.setStatus(request.status());
    order = orderRepository.save(order);
    log.info(
        "Updated status for order {} to {} in organization {}",
        id,
        request.status(),
        organizationId);

    applicationEventPublisher.publishEvent(
        new OrderStatusChangedEvent(order.getId(), oldStatus, request.status()));

    if (oldStatus != OrderStatus.CONFIRMED && request.status() == OrderStatus.CONFIRMED) {
      salesOrderIntegrationService.handleOrderConfirmation(order, request.warehouseId());
    }

    // Advance the linked lead's stage when the order reaches a terminal state.
    // CONFIRMED → PROPOSAL, CANCELLED → LOST, COMPLETED → WON.
    if (order.getLead() != null) {
      LeadStage targetLeadStage =
          switch (request.status()) {
            case CONFIRMED -> LeadStage.PROPOSAL;
            case CANCELLED -> LeadStage.LOST;
            case COMPLETED -> LeadStage.WON;
            default -> null;
          };
      if (targetLeadStage != null) {
        Lead lead = order.getLead();
        lead.setStage(targetLeadStage);
        leadRepository.save(lead);
        log.info(
            "Advanced lead {} to {} after order {} was {}",
            lead.getId(),
            targetLeadStage,
            id,
            request.status());
      }
    }

    return enrichWithWarehouse(orderMapper.toResponse(order));
  }

  @Override
  @Transactional
  public void deleteQuotation(UUID organizationId, UUID id) {
    Order order = findOrderShallowByIdAndOrganizationId(id, organizationId);

    securityAuthService.isOrderOwnerOrManagerOrAdmin(order, SecurityUtils.getCurrentUser());

    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Only quotations in DRAFT status can be deleted");
    }
    orderRepository.delete(order);
    log.info("Deleted quotation {} from organization {}", id, organizationId);
  }

  // ---- Private helpers ----

  /**
   * Returns the user-supplied order number if it is non-blank, otherwise generates a unique one
   * with the format {@code QUO-YYYYMMDD-XXXXXXXX} (8 upper-case hex characters from a random UUID).
   * Uniqueness within the organisation is guaranteed by retrying until no collision is found.
   */
  private String resolveOrderNumber(String requested, UUID organizationId) {
    if (requested != null && !requested.isBlank()) {
      if (orderRepository.existsByOrganizationIdAndOrderNumber(organizationId, requested)) {
        throw new BadRequestException("Order number already exists in this organization");
      }
      return requested;
    }

    String datePart = LocalDate.now(ZoneOffset.UTC).toString().replace("-", ""); // e.g. "20260608"
    String generated;
    do {
      String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
      generated = "QUO-" + datePart + "-" + shortId; // e.g. "QUO-20260608-A3F7C291"
    } while (orderRepository.existsByOrganizationIdAndOrderNumber(organizationId, generated));

    return generated;
  }

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Order findOrderByIdAndOrganizationId(UUID orderId, UUID organizationId) {
    return orderRepository
        .findByIdAndOrganizationId(orderId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
  }

  /**
   * Resolves an order without fetching the items collection — use for writes that don't need items.
   */
  private Order findOrderShallowByIdAndOrganizationId(UUID orderId, UUID organizationId) {
    return orderRepository
        .findShallowByIdAndOrganizationId(orderId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
  }

  private Order findOrderWithLeadByIdAndOrganizationId(UUID orderId, UUID organizationId) {
    return orderRepository
        .findWithLeadByIdAndOrganizationId(orderId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
  }

  private Lead findLeadByIdAndOrganizationId(UUID leadId, UUID organizationId) {
    return leadRepository
        .findByIdAndOrganizationId(leadId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));
  }

  private OrderResponse enrichWithWarehouse(OrderResponse response) {
    if (response == null || response.id() == null) {
      return response;
    }
    List<com.dut.erp.entity.InventoryDocument> activeDocs = inventoryDocumentRepository.findActiveDocuments(
        ReferenceType.SALES_ORDER, response.id(), DocumentType.ISSUE
    );
    UUID warehouseId = null;
    String warehouseName = null;
    if (!activeDocs.isEmpty()) {
      com.dut.erp.entity.InventoryDocument doc = activeDocs.get(0);
      warehouseId = doc.getWarehouse().getId();
      warehouseName = doc.getWarehouse().getName();
    }

    java.util.Optional<com.dut.erp.entity.Invoice> optInvoice = invoiceRepository.findByOrderIdAndOrganizationId(
        response.id(), response.organization().id()
    );
    UUID invoiceId = optInvoice.map(com.dut.erp.entity.Invoice::getId).orElse(null);
    String invoiceNumber = optInvoice.map(com.dut.erp.entity.Invoice::getInvoiceNumber).orElse(null);
    com.dut.erp.enums.InvoiceStatus invoiceStatus = optInvoice.map(com.dut.erp.entity.Invoice::getStatus).orElse(null);

    return new OrderResponse(
        response.id(),
        response.organization(),
        response.partner(),
        response.lead(),
        response.orderNumber(),
        response.status(),
        response.deliveryDate(),
        response.expirationDate(),
        response.totalAmount(),
        response.items(),
        response.createdAt(),
        response.updatedAt(),
        response.createdBy(),
        response.updatedBy(),
        warehouseId,
        warehouseName,
        invoiceId,
        invoiceNumber,
        invoiceStatus,
        response.saleTeamId(),
        response.saleTeamName()
    );
  }
}
