package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateSaleInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.RegisterPaymentRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleInvoiceBaseResponse;
import com.dut.erp.dto.response.SaleInvoiceResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.SaleInvoice;
import com.dut.erp.entity.SaleOrder;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.SaleOrderStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.SaleInvoiceMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.SaleInvoiceRepository;
import com.dut.erp.repository.SaleOrderRepository;
import com.dut.erp.service.SaleInvoiceService;
import java.math.BigDecimal;
import java.time.Instant;
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
public class SaleInvoiceServiceImpl implements SaleInvoiceService {

  private final OrganizationRepository organizationRepository;
  private final SaleInvoiceRepository saleInvoiceRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final SaleInvoiceMapper saleInvoiceMapper;

  @Override
  public PagedEntityResponse<SaleInvoiceBaseResponse> getInvoicesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching sale invoices for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("invoiceDate"), SortField.asc("updatedAt")));

    Page<UUID> ids = saleInvoiceRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, SaleInvoice> invoiceMap =
        saleInvoiceRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(SaleInvoice::getId, Function.identity()));

    List<SaleInvoiceBaseResponse> responses =
        ids.getContent().stream()
            .map(invoiceMap::get)
            .filter(Objects::nonNull)
            .map(saleInvoiceMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public SaleInvoiceResponse getInvoiceById(UUID organizationId, UUID invoiceId) {
    log.info("Fetching sale invoice {} for organization {}", invoiceId, organizationId);
    SaleInvoice invoice = findInvoiceByIdAndVerifyOrganization(invoiceId, organizationId);
    return saleInvoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public SaleInvoiceResponse createInvoice(UUID organizationId, CreateSaleInvoiceRequest request) {
    Organization organization = findOrganizationById(organizationId);
    SaleOrder order = findOrderByIdAndVerifyOrganization(request.orderId(), organizationId);

    if (order.getStatus() != SaleOrderStatus.CONFIRMED) {
      throw new BadRequestException(
          "Invoice can only be created from a CONFIRMED sale order. Current status: "
              + order.getStatus());
    }

    if (request.dueDate().isBefore(request.invoiceDate())) {
      throw new BadRequestException("Due date must be after invoice date.");
    }

    String invoiceNumber = generateInvoiceNumber(organizationId);

    SaleInvoice invoice =
        SaleInvoice.builder()
            .organization(organization)
            .order(order)
            .partner(order.getPartner())
            .invoiceNumber(invoiceNumber)
            .invoiceDate(request.invoiceDate())
            .dueDate(request.dueDate())
            .totalAmount(order.getTotalAmount())
            .build();

    invoice = saleInvoiceRepository.save(invoice);
    log.info("Created invoice {} for order {}", invoice.getId(), request.orderId());
    return saleInvoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public SaleInvoiceResponse postInvoice(UUID organizationId, UUID invoiceId) {
    findOrganizationById(organizationId);
    SaleInvoice invoice = findInvoiceByIdAndVerifyOrganization(invoiceId, organizationId);

    if (invoice.getStatus() != InvoiceStatus.DRAFT) {
      throw new BadRequestException(
          "Only DRAFT invoices can be posted. Current status: " + invoice.getStatus());
    }

    invoice.setStatus(InvoiceStatus.POSTED);
    invoice = saleInvoiceRepository.save(invoice);
    log.info("Posted invoice {} in organization {}", invoiceId, organizationId);
    return saleInvoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public SaleInvoiceResponse registerPayment(
      UUID organizationId, UUID invoiceId, RegisterPaymentRequest request) {
    findOrganizationById(organizationId);
    SaleInvoice invoice = findInvoiceByIdAndVerifyOrganization(invoiceId, organizationId);

    if (invoice.getStatus() == InvoiceStatus.PAID
        || invoice.getStatus() == InvoiceStatus.CANCELLED) {
      throw new BadRequestException(
          "Cannot register payment for invoice with status: " + invoice.getStatus());
    }

    BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.amount());

    if (newPaidAmount.compareTo(invoice.getTotalAmount()) > 0) {
      throw new BadRequestException(
          "Payment amount exceeds remaining balance. Remaining: "
              + invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
    }

    invoice.setPaidAmount(newPaidAmount);

    // Auto-update status based on payment
    if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
      invoice.setStatus(InvoiceStatus.PAID);
    } else {
      invoice.setStatus(InvoiceStatus.PARTIAL_PAID);
    }

    invoice = saleInvoiceRepository.save(invoice);
    log.info(
        "Registered payment of {} for invoice {}. Status now: {}",
        request.amount(),
        invoiceId,
        invoice.getStatus());
    return saleInvoiceMapper.toResponse(invoice);
  }

  @Override
  @Transactional
  public void cancelInvoice(UUID organizationId, UUID invoiceId) {
    findOrganizationById(organizationId);
    SaleInvoice invoice = findInvoiceByIdAndVerifyOrganization(invoiceId, organizationId);

    if (invoice.getStatus() == InvoiceStatus.PAID) {
      throw new BadRequestException("Paid invoices cannot be cancelled.");
    }

    invoice.setStatus(InvoiceStatus.CANCELLED);
    saleInvoiceRepository.save(invoice);
    log.info("Cancelled invoice {} in organization {}", invoiceId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organization not found with id: " + organizationId));
  }

  private SaleInvoice findInvoiceByIdAndVerifyOrganization(UUID invoiceId, UUID organizationId) {
    SaleInvoice invoice =
        saleInvoiceRepository
            .findByIdWithDetails(invoiceId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Sale invoice not found with id: " + invoiceId));
    if (!invoice.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Invoice does not belong to the specified organization.");
    }
    return invoice;
  }

  private SaleOrder findOrderByIdAndVerifyOrganization(UUID orderId, UUID organizationId) {
    SaleOrder order =
        saleOrderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sale order not found with id: " + orderId));
    if (!order.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Sale order does not belong to the specified organization.");
    }
    return order;
  }

  private String generateInvoiceNumber(UUID organizationId) {
    String candidate = "INV-" + Instant.now().toEpochMilli();
    if (saleInvoiceRepository
        .findByInvoiceNumberAndOrganizationId(candidate, organizationId)
        .isPresent()) {
      candidate = candidate + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
    return candidate;
  }
}
