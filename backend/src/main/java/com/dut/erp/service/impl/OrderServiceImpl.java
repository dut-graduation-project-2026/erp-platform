package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateOrderStatusRequest;
import com.dut.erp.dto.request.UpsertOrderRequest;
import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.Invoice;
import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrderMapper;
import com.dut.erp.repository.InvoiceRepository;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.OrderService;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

  private final OrganizationRepository organizationRepository;
  private final OrderRepository orderRepository;
  private final LeadRepository leadRepository;
  private final OrderMapper orderMapper;
  private final InvoiceRepository invoiceRepository;

  @Override
  public PagedEntityResponse<OrderBaseResponse> getQuotationsWithFilterByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching quotations for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? orderRepository.findQuotationIdsByOrganizationIdAndSearch(
                organizationId, search, pageable)
            : orderRepository.findQuotationIdsByOrganizationId(organizationId, pageable);

    return getPagedResponseFromIds(ids, pageable);
  }

  @Override
  public PagedEntityResponse<OrderBaseResponse> getOrdersWithFilterByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching orders for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? orderRepository.findOrderIdsByOrganizationIdAndSearch(
                organizationId, search, pageable)
            : orderRepository.findOrderIdsByOrganizationId(organizationId, pageable);

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

    List<OrderBaseResponse> responses =
        ids.getContent().stream()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .map(orderMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public OrderResponse getQuotationById(UUID organizationId, UUID id) {
    log.info("Fetching quotation {} for organization {}", id, organizationId);
    Order order = findOrderByIdAndOrganizationId(id, organizationId);
    if (order.getStatus() != OrderStatus.DRAFT) {
      throw new BadRequestException("Requested resource is an Order, not a Quotation");
    }
    return orderMapper.toResponse(order);
  }

  @Override
  public OrderResponse getOrderById(UUID organizationId, UUID id) {
    log.info("Fetching order {} for organization {}", id, organizationId);
    Order order = findOrderByIdAndOrganizationId(id, organizationId);
    if (order.getStatus() == OrderStatus.DRAFT) {
      throw new BadRequestException("Requested resource is a Quotation, not an Order");
    }
    return orderMapper.toResponse(order);
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

    return orderMapper.toResponse(order);
  }

  @Override
  @Transactional
  public OrderResponse updateQuotation(UUID organizationId, UUID id, UpsertOrderRequest request) {
    Order order = findOrderByIdAndOrganizationId(id, organizationId);

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
    return orderMapper.toResponse(order);
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(
      UUID organizationId, UUID id, UpdateOrderStatusRequest request) {
    Order order = findOrderWithLeadByIdAndOrganizationId(id, organizationId);

    // Rule: Once status is COMPLETED, it cannot be updated
    if (order.getStatus() == OrderStatus.COMPLETED) {
      throw new BadRequestException("Cannot update status of a COMPLETED order");
    }

    // Rule: Once status changes away from DRAFT, it cannot transition back to DRAFT
    if (order.getStatus() != OrderStatus.DRAFT && request.status() == OrderStatus.DRAFT) {
      throw new BadRequestException("Cannot revert an Order back to a DRAFT Quotation");
    }

    // Rule: Only when the invoice is PAID can the order be completed/done
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
    }

    // Rule: If order is CANCELLED, cancel the linked invoice as well
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
    }

    order.setStatus(request.status());
    order = orderRepository.save(order);
    log.info(
        "Updated status for order {} to {} in organization {}",
        id,
        request.status(),
        organizationId);

    // Advance the linked lead's stage when the order reaches a terminal state.
    // CONFIRMED → PROPOSAL, CANCELLED → LOST. SENT → WON, COMPLETED → WON.
    if (order.getLead() != null) {
      LeadStage targetLeadStage =
          switch (request.status()) {
            case CONFIRMED -> LeadStage.PROPOSAL;
            case CANCELLED -> LeadStage.LOST;
            case COMPLETED -> LeadStage.WON;
            case SENT -> LeadStage.WON;
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

    return orderMapper.toResponse(order);
  }

  @Override
  @Transactional
  public void deleteQuotation(UUID organizationId, UUID id) {
    Order order = findOrderShallowByIdAndOrganizationId(id, organizationId);
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
}
