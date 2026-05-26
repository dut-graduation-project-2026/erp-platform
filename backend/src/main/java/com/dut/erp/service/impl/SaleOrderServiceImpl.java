package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateSaleOrderRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.SaleOrderLineRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleOrderBaseResponse;
import com.dut.erp.dto.response.SaleOrderResponse;
import com.dut.erp.entity.CrmLead;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.SaleOrder;
import com.dut.erp.entity.SaleOrderLine;
import com.dut.erp.entity.SalePartner;
import com.dut.erp.entity.User;
import com.dut.erp.enums.SaleOrderStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.SaleOrderMapper;
import com.dut.erp.repository.CrmLeadRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.SaleOrderRepository;
import com.dut.erp.repository.SalePartnerRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.SaleOrderService;
import com.dut.erp.dto.request.CreateStockMoveRequest;
import com.dut.erp.dto.request.CreateStockPickingRequest;
import com.dut.erp.enums.PickingType;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.service.StockPickingService;
import org.springframework.context.annotation.Lazy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
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
@Transactional(readOnly = true)
public class SaleOrderServiceImpl implements SaleOrderService {

  private final OrganizationRepository organizationRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final SalePartnerRepository salePartnerRepository;
  private final ProductRepository productRepository;
  private final CrmLeadRepository crmLeadRepository;
  private final UserRepository userRepository;
  private final StockLocationRepository stockLocationRepository;
  private final StockPickingService stockPickingService;
  private final SaleOrderMapper saleOrderMapper;

  public SaleOrderServiceImpl(
      OrganizationRepository organizationRepository,
      SaleOrderRepository saleOrderRepository,
      SalePartnerRepository salePartnerRepository,
      ProductRepository productRepository,
      CrmLeadRepository crmLeadRepository,
      UserRepository userRepository,
      StockLocationRepository stockLocationRepository,
      @Lazy StockPickingService stockPickingService,
      SaleOrderMapper saleOrderMapper) {
    this.organizationRepository = organizationRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.salePartnerRepository = salePartnerRepository;
    this.productRepository = productRepository;
    this.crmLeadRepository = crmLeadRepository;
    this.userRepository = userRepository;
    this.stockLocationRepository = stockLocationRepository;
    this.stockPickingService = stockPickingService;
    this.saleOrderMapper = saleOrderMapper;
  }

  @Override
  public PagedEntityResponse<SaleOrderBaseResponse> getOrdersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching sale orders for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("updatedAt"), SortField.asc("orderDate")));

    Page<UUID> ids = saleOrderRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, SaleOrder> orderMap =
        saleOrderRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(SaleOrder::getId, Function.identity()));

    List<SaleOrderBaseResponse> responses =
        ids.getContent().stream()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .map(saleOrderMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public SaleOrderResponse getOrderById(UUID organizationId, UUID orderId) {
    log.info("Fetching sale order {} for organization {}", orderId, organizationId);
    SaleOrder order = findOrderByIdAndVerifyOrganization(orderId, organizationId);
    return saleOrderMapper.toResponse(order);
  }

  @Override
  @Transactional
  public SaleOrderResponse createOrder(UUID organizationId, CreateSaleOrderRequest request) {
    Organization organization = findOrganizationById(organizationId);
    SalePartner partner = findPartnerByIdAndVerifyOrganization(request.partnerId(), organizationId);

    String orderNumber = generateOrderNumber(organizationId);

    SaleOrder.SaleOrderBuilder builder =
        SaleOrder.builder()
            .organization(organization)
            .partner(partner)
            .orderNumber(orderNumber)
            .orderDate(request.orderDate());

    if (request.opportunityId() != null) {
      CrmLead opportunity =
          crmLeadRepository
              .findById(request.opportunityId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Opportunity not found: " + request.opportunityId()));
      builder.opportunity(opportunity);
    }

    if (request.salespersonId() != null) {
      User salesperson =
          userRepository
              .findById(request.salespersonId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "User not found: " + request.salespersonId()));
      builder.salesperson(salesperson);
    }

    SaleOrder order = builder.build();
    List<SaleOrderLine> lines = buildOrderLines(request.lines(), order);
    order.setLines(lines);
    order.setTotalAmount(calculateTotal(lines));

    order = saleOrderRepository.save(order);
    log.info("Created sale order {} in organization {}", order.getId(), organizationId);
    return saleOrderMapper.toResponse(order);
  }

  @Override
  @Transactional
  public SaleOrderResponse updateOrder(
      UUID organizationId, UUID orderId, CreateSaleOrderRequest request) {
    findOrganizationById(organizationId);
    SaleOrder order = findOrderByIdAndVerifyOrganization(orderId, organizationId);

    SaleOrderStatus currentStatus = order.getStatus();
    SaleOrderStatus targetStatus = request.status() != null ? request.status() : currentStatus;

    if (currentStatus != targetStatus) {
      if (targetStatus == SaleOrderStatus.CONFIRMED) {
        if (currentStatus != SaleOrderStatus.DRAFT && currentStatus != SaleOrderStatus.SENT) {
          throw new BadRequestException(
              "Only DRAFT or SENT orders can be confirmed. Current status: " + currentStatus);
        }
        createDeliveryOrder(order, organizationId);
      } else if (targetStatus == SaleOrderStatus.CANCELLED) {
        if (currentStatus == SaleOrderStatus.CONFIRMED) {
          throw new BadRequestException("Confirmed orders cannot be cancelled directly.");
        }
      }
      order.setStatus(targetStatus);
    }

    if (currentStatus != SaleOrderStatus.DRAFT) {
      boolean hasChanges = !order.getPartner().getId().equals(request.partnerId())
          || !order.getOrderDate().equals(request.orderDate());
      if (hasChanges) {
        throw new BadRequestException("Only DRAFT orders can be modified. Current status: " + currentStatus);
      }
    } else {
      SalePartner partner = findPartnerByIdAndVerifyOrganization(request.partnerId(), organizationId);
      order.setPartner(partner);
      order.setOrderDate(request.orderDate());

      order.getLines().clear();
      List<SaleOrderLine> lines = buildOrderLines(request.lines(), order);
      order.getLines().addAll(lines);
      order.setTotalAmount(calculateTotal(lines));
    }

    order = saleOrderRepository.save(order);
    log.info("Updated sale order {} in organization {}", orderId, organizationId);
    return saleOrderMapper.toResponse(order);
  }

  private void createDeliveryOrder(SaleOrder order, UUID organizationId) {
    log.info("Auto-creating delivery order for sale order {}", order.getOrderNumber());

    StockLocation sourceLoc = stockLocationRepository.findAll().stream()
        .filter(l -> l.getWarehouse().getOrganization().getId().equals(organizationId)
            && l.getLocationType() == com.dut.erp.enums.LocationType.INTERNAL)
        .findFirst()
        .orElseThrow(() -> new BadRequestException("No internal stock location found in organization to pick goods from."));

    StockLocation destLoc = stockLocationRepository.findAll().stream()
        .filter(l -> l.getWarehouse().getOrganization().getId().equals(organizationId)
            && l.getLocationType() == com.dut.erp.enums.LocationType.CUSTOMER)
        .findFirst()
        .orElseGet(() -> {
          StockLocation newCustLoc = StockLocation.builder()
              .warehouse(sourceLoc.getWarehouse())
              .name("Customer Location")
              .code("CUSTOMER")
              .locationType(com.dut.erp.enums.LocationType.CUSTOMER)
              .isActive(true)
              .build();
          return stockLocationRepository.save(newCustLoc);
        });

    List<CreateStockMoveRequest> moves = order.getLines().stream()
        .map(line -> new CreateStockMoveRequest(
            line.getProduct().getId(),
            line.getQuantity(),
            null
        ))
        .collect(Collectors.toList());

    CreateStockPickingRequest request = new CreateStockPickingRequest(
        PickingType.OUTGOING,
        sourceLoc.getId(),
        destLoc.getId(),
        order.getPartner().getId(),
        order.getId(),
        null,
        Instant.now(),
        moves
    );

    stockPickingService.createPicking(organizationId, request);
    log.info("Auto-created outgoing picking for SO {}", order.getOrderNumber());
  }

  // ---- Private helpers ----

  private List<SaleOrderLine> buildOrderLines(List<SaleOrderLineRequest> lineRequests, SaleOrder order) {
    List<SaleOrderLine> lines = new ArrayList<>();
    for (SaleOrderLineRequest req : lineRequests) {
      Product product =
          productRepository
              .findById(req.productId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Product not found: " + req.productId()));

      BigDecimal discount = req.discountPercent() != null ? req.discountPercent() : BigDecimal.ZERO;
      BigDecimal subtotal =
          req.unitPrice()
              .multiply(req.quantity())
              .multiply(BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
              .setScale(2, RoundingMode.HALF_UP);

      lines.add(
          SaleOrderLine.builder()
              .order(order)
              .product(product)
              .quantity(req.quantity())
              .unitPrice(req.unitPrice())
              .discountPercent(discount)
              .subtotal(subtotal)
              .build());
    }
    return lines;
  }

  private BigDecimal calculateTotal(List<SaleOrderLine> lines) {
    return lines.stream()
        .map(SaleOrderLine::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private String generateOrderNumber(UUID organizationId) {
    // Simple sequential number format: SO-{timestamp}
    String candidate = "SO-" + Instant.now().toEpochMilli();
    if (saleOrderRepository.findByOrderNumberAndOrganizationId(candidate, organizationId).isPresent()) {
      candidate = candidate + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
    return candidate;
  }

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organization not found with id: " + organizationId));
  }

  private SaleOrder findOrderByIdAndVerifyOrganization(UUID orderId, UUID organizationId) {
    SaleOrder order =
        saleOrderRepository
            .findByIdWithDetails(orderId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sale order not found with id: " + orderId));
    if (!order.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Sale order does not belong to the specified organization.");
    }
    return order;
  }

  private SalePartner findPartnerByIdAndVerifyOrganization(UUID partnerId, UUID organizationId) {
    SalePartner partner =
        salePartnerRepository
            .findById(partnerId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Sale partner not found: " + partnerId));
    if (!partner.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Partner does not belong to the specified organization.");
    }
    return partner;
  }
}
