package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateInvoiceStatusRequest;
import com.dut.erp.dto.response.InvoiceBaseResponse;
import com.dut.erp.dto.response.InvoiceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.Invoice;
import com.dut.erp.entity.Order;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.ReferenceType;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.InvoiceMapper;
import com.dut.erp.repository.InvoiceRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.service.InvoiceService;
import com.dut.erp.dto.event.InvoiceStatusChangedEvent;
import com.dut.erp.dto.event.OrderStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
public class InvoiceServiceImpl implements InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final OrderRepository orderRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final InvoiceMapper invoiceMapper;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  public InvoiceResponse createInvoiceFromOrder(UUID organizationId, CreateInvoiceRequest request) {
    log.info(
        "Creating invoice from order {} for organization {}", request.orderId(), organizationId);

    Order order =
        orderRepository
            .findShallowByIdAndOrganizationId(request.orderId(), organizationId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Order not found with id: " + request.orderId()));

    if (order.getStatus() == OrderStatus.DRAFT) {
      throw new BadRequestException("Cannot create invoice from a DRAFT order");
    }

    boolean hasActivePaymentInvoice =
        invoiceRepository.existsByOrderIdAndStatusIn(
            request.orderId(), List.of(InvoiceStatus.PAID, InvoiceStatus.PARTIAL_PAID));
    if (hasActivePaymentInvoice) {
      throw new BadRequestException(
          "Cannot create invoice because a PAID or PARTIAL_PAID invoice already exists for this"
              + " order");
    }

    String invoiceNumber = generateUniqueInvoiceNumber(organizationId);
    Instant now = Instant.now();
    Instant dueDate = request.dueDate() != null ? request.dueDate() : now.plus(30, ChronoUnit.DAYS);

    Invoice invoice =
        Invoice.builder()
            .organization(order.getOrganization())
            .order(order)
            .partner(order.getPartner())
            .invoiceNumber(invoiceNumber)
            .dueDate(dueDate)
            .totalAmount(order.getTotalAmount())
            .paidAmount(java.math.BigDecimal.ZERO)
            .status(InvoiceStatus.DRAFT)
            .build();

    invoice = invoiceRepository.save(invoice);
    log.info("Successfully created invoice {} from order {}", invoice.getId(), order.getId());

    applicationEventPublisher.publishEvent(new InvoiceStatusChangedEvent(invoice.getId(), null, InvoiceStatus.DRAFT));

    return invoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public InvoiceResponse updateInvoiceStatus(
      UUID organizationId, UUID id, UpdateInvoiceStatusRequest request) {
    log.info(
        "Updating status of invoice {} to {} in organization {}",
        id,
        request.status(),
        organizationId);

    Invoice invoice =
        invoiceRepository
            .findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

    if (invoice.getOrder() != null && invoice.getOrder().getStatus() == OrderStatus.CANCELLED) {
      throw new BadRequestException(
          "Cannot update invoice status when the linked order is CANCELLED");
    }

    InvoiceStatus currentStatus = invoice.getStatus();
    InvoiceStatus newStatus = request.status();

    if (currentStatus != newStatus) {
      if (newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.PARTIAL_PAID) {
        boolean existsOther =
            invoiceRepository.existsByOrderIdAndStatusInAndIdNot(
                invoice.getOrder().getId(),
                List.of(InvoiceStatus.PAID, InvoiceStatus.PARTIAL_PAID),
                invoice.getId());
        if (existsOther) {
          throw new BadRequestException(
              "Cannot mark invoice as PAID or PARTIAL_PAID because another invoice for this order"
                  + " is already PAID or PARTIAL_PAID");
        }
      }
      if (currentStatus == InvoiceStatus.PAID) {
        throw new BadRequestException("Cannot change status of a PAID invoice");
      }
      if (currentStatus == InvoiceStatus.CANCELLED) {
        throw new BadRequestException("Cannot change status of a CANCELLED invoice");
      }
      if (currentStatus != InvoiceStatus.DRAFT && newStatus == InvoiceStatus.DRAFT) {
        throw new BadRequestException("Cannot revert invoice status back to DRAFT");
      }
      if (currentStatus == InvoiceStatus.PARTIAL_PAID && newStatus == InvoiceStatus.POSTED) {
        throw new BadRequestException("Cannot revert status from PARTIAL_PAID back to POSTED");
      }
    }

    invoice.setStatus(newStatus);
    invoice = invoiceRepository.save(invoice);

    applicationEventPublisher.publishEvent(new InvoiceStatusChangedEvent(invoice.getId(), currentStatus, newStatus));

    if (newStatus == InvoiceStatus.PAID) {
      checkAndCompleteOrder(invoice.getOrder());
    }

    return invoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public InvoiceResponse registerPayment(UUID organizationId, UUID id, com.dut.erp.dto.request.RegisterPaymentRequest request) {
    log.info("Registering payment of {} for invoice {} in organization {}", request.amount(), id, organizationId);

    Invoice invoice = invoiceRepository
        .findByIdAndOrganizationId(id, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

    if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED || invoice.getStatus() == InvoiceStatus.DRAFT) {
        throw new BadRequestException("Cannot register payment for invoice in status: " + invoice.getStatus());
    }

    java.math.BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.amount());
    if (newPaidAmount.compareTo(invoice.getTotalAmount()) > 0) {
        throw new BadRequestException("Payment amount exceeds remaining balance");
    }

    InvoiceStatus oldStatus = invoice.getStatus();
    InvoiceStatus newStatus = (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) ? InvoiceStatus.PAID : InvoiceStatus.PARTIAL_PAID;

    invoice.setPaidAmount(newPaidAmount);
    invoice.setStatus(newStatus);
    invoice = invoiceRepository.save(invoice);

    if (oldStatus != newStatus) {
        applicationEventPublisher.publishEvent(new InvoiceStatusChangedEvent(invoice.getId(), oldStatus, newStatus));
        if (newStatus == InvoiceStatus.PAID) {
            checkAndCompleteOrder(invoice.getOrder());
        }
    }
    return invoiceMapper.toResponse(invoice);
  }

  @Override
  public InvoiceResponse getInvoiceById(UUID organizationId, UUID id) {
    log.info("Fetching invoice {} for organization {}", id, organizationId);
    Invoice invoice =
        invoiceRepository
            .findByIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

    return invoiceMapper.toResponse(invoice);
  }

  @Override
  public InvoiceResponse getInvoiceByOrderId(UUID organizationId, UUID orderId) {
    log.info("Fetching invoice for order {} and organization {}", orderId, organizationId);
    Invoice invoice =
        invoiceRepository
            .findByOrderIdAndOrganizationId(orderId, organizationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Invoice not found for order id: " + orderId));

    return invoiceMapper.toResponse(invoice);
  }

  @Override
  public PagedEntityResponse<InvoiceBaseResponse> getInvoices(
      UUID organizationId, String search, String status, PaginationRequest paginationRequest) {
    log.info("Fetching invoices for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit());

    InvoiceStatus invoiceStatus = null;
    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
      try {
        invoiceStatus = InvoiceStatus.valueOf(status.trim().toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status value: {}", status);
      }
    }

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? invoiceRepository.findInvoiceIdsByOrganizationIdAndSearch(
                organizationId, search, invoiceStatus, pageable)
            : invoiceRepository.findInvoiceIdsByOrganizationId(organizationId, invoiceStatus, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Invoice> invoiceMap =
        invoiceRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Invoice::getId, Function.identity()));

    List<InvoiceBaseResponse> responses =
        ids.getContent().stream()
            .map(invoiceMap::get)
            .filter(Objects::nonNull)
            .map(invoiceMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  private String generateUniqueInvoiceNumber(UUID organizationId) {
    String datePart = LocalDate.now(ZoneOffset.UTC).toString().replace("-", ""); // e.g. "20260608"
    String generated;
    do {
      String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
      generated = "INV-" + datePart + "-" + shortId; // e.g. "INV-20260608-A3F7C291"
    } while (invoiceRepository.existsByOrganizationIdAndInvoiceNumber(organizationId, generated));

    return generated;
  }

  private void checkAndCompleteOrder(Order order) {
    if (order == null) return;

    // 1. Check if the linked invoice is paid
    boolean isInvoicePaid = invoiceRepository.findByOrderIdAndOrganizationId(order.getId(), order.getOrganization().getId())
        .map(inv -> inv.getStatus() == InvoiceStatus.PAID)
        .orElse(false);

    // 2. Check if the active warehouse issue document is completed or sent
    boolean isDeliveryCompleted = inventoryDocumentRepository
        .findByReferenceTypeAndReferenceIdAndDocumentType(
            ReferenceType.SALES_ORDER, order.getId(), DocumentType.ISSUE)
        .map(doc -> doc.getDocumentStatus() == DocumentStatus.COMPLETED || doc.getDocumentStatus() == DocumentStatus.SENT)
        .orElse(false);

    // 3. If both are completed/paid -> Close the order
    if (isInvoicePaid && isDeliveryCompleted) {
      OrderStatus oldStatus = order.getStatus();
      if (oldStatus != OrderStatus.COMPLETED) {
        order.setStatus(OrderStatus.COMPLETED);
        if (order.getLead() != null && order.getLead().getStage() != LeadStage.WON) {
          order.getLead().setStage(LeadStage.WON);
        }
        orderRepository.save(order);
        log.info("Automatically completed order {} because both delivery and payment are completed.", order.getOrderNumber());
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.COMPLETED));
      }
    } else if (isDeliveryCompleted) {
      OrderStatus oldStatus = order.getStatus();
      if (oldStatus != OrderStatus.SENT && oldStatus != OrderStatus.COMPLETED) {
        order.setStatus(OrderStatus.SENT);
        orderRepository.save(order);
        log.info("Order {} transitioned to SENT status because delivery is sent but invoice is not paid.", order.getOrderNumber());
        applicationEventPublisher.publishEvent(new OrderStatusChangedEvent(order.getId(), oldStatus, OrderStatus.SENT));
      }
    }
  }
}
