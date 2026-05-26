package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateStockMoveRequest;
import com.dut.erp.dto.request.CreateStockPickingRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockPickingBaseResponse;
import com.dut.erp.dto.response.StockPickingResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.SalePartner;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.StockLot;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockPicking;
import com.dut.erp.enums.PickingType;
import com.dut.erp.enums.StockMoveState;
import com.dut.erp.enums.StockPickingState;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockPickingMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.SalePartnerRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.StockLotRepository;
import com.dut.erp.repository.StockMoveRepository;
import com.dut.erp.enums.CostMethod;
import com.dut.erp.repository.StockPickingRepository;
import com.dut.erp.repository.SaleOrderRepository;
import com.dut.erp.service.ReservationService;
import com.dut.erp.service.StockPickingService;
import com.dut.erp.service.StockValuationService;
import com.dut.erp.service.StockQuantService;
import java.math.BigDecimal;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockPickingServiceImpl implements StockPickingService {

  private final OrganizationRepository organizationRepository;
  private final StockLocationRepository stockLocationRepository;
  private final SalePartnerRepository salePartnerRepository;
  private final ProductRepository productRepository;
  private final StockLotRepository stockLotRepository;
  private final StockPickingRepository stockPickingRepository;
  private final StockMoveRepository stockMoveRepository;
  private final SaleOrderRepository saleOrderRepository;
  private final ReservationService reservationService;
  private final StockQuantService stockQuantService;
  private final StockValuationService stockValuationService;
  private final StockPickingMapper stockPickingMapper;

  @Override
  public PagedEntityResponse<StockPickingBaseResponse> getPickings(
      UUID organizationId, PickingType pickingType, PaginationRequest paginationRequest) {
    log.info("Fetching pickings for organization {} with type {}", organizationId, pickingType);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("createdAt")));

    Page<UUID> ids = stockPickingRepository.findIdsByOrganizationIdAndPickingType(
        organizationId, pickingType, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, StockPicking> pickingMap =
        stockPickingRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(StockPicking::getId, Function.identity()));

    List<StockPickingBaseResponse> responses =
        ids.getContent().stream()
            .map(pickingMap::get)
            .filter(Objects::nonNull)
            .map(stockPickingMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public StockPickingResponse getPickingById(UUID organizationId, UUID pickingId) {
    StockPicking picking = findPickingByIdAndVerifyOrganization(pickingId, organizationId);
    return stockPickingMapper.toResponse(picking);
  }

  @Override
  @Transactional
  public StockPickingResponse createPicking(UUID organizationId, CreateStockPickingRequest request) {
    log.info("Creating picking type {} in org {}", request.pickingType(), organizationId);
    Organization organization = findOrganizationById(organizationId);
    StockLocation location = stockLocationRepository.findById(request.locationId())
        .orElseThrow(() -> new ResourceNotFoundException("Source location not found"));
    StockLocation locationDest = stockLocationRepository.findById(request.locationDestId())
        .orElseThrow(() -> new ResourceNotFoundException("Destination location not found"));

    SalePartner partner = null;
    if (request.partnerId() != null) {
      partner = salePartnerRepository.findById(request.partnerId())
          .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
    }

    // Name sequence generation
    long count = stockPickingRepository.countByPickingTypeAndOrganizationId(request.pickingType(), organizationId);
    String prefix = switch (request.pickingType()) {
      case INCOMING -> "IN";
      case OUTGOING -> "OUT";
      case INTERNAL -> "INT";
    };
    String name = String.format("WH/%s/%05d", prefix, count + 1);

    StockPicking picking = StockPicking.builder()
        .organization(organization)
        .name(name)
        .pickingType(request.pickingType())
        .location(location)
        .locationDest(locationDest)
        .partner(partner)
        .saleOrder(request.saleOrderId() != null ? saleOrderRepository.findById(request.saleOrderId()).orElse(null) : null)
        .purchaseOrderId(request.purchaseOrderId())
        .scheduledDate(request.scheduledDate() != null ? request.scheduledDate() : Instant.now())
        .state(StockPickingState.DRAFT)
        .stockMoves(new ArrayList<>())
        .build();

    picking = stockPickingRepository.save(picking);

    for (CreateStockMoveRequest moveReq : request.stockMoves()) {
      Product product = productRepository.findById(moveReq.productId())
          .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + moveReq.productId()));
      
      StockLot lot = null;
      if (moveReq.lotId() != null) {
        lot = stockLotRepository.findById(moveReq.lotId())
            .orElseThrow(() -> new ResourceNotFoundException("Lot not found: " + moveReq.lotId()));
      }

      StockMove move = StockMove.builder()
          .picking(picking)
          .product(product)
          .location(location)
          .locationDest(locationDest)
          .lot(lot)
          .productUomQty(moveReq.productUomQty())
          .quantityDone(BigDecimal.ZERO)
          .state(StockMoveState.DRAFT)
          .build();

      picking.getStockMoves().add(move);
    }

    picking = stockPickingRepository.save(picking);
    return stockPickingMapper.toResponse(picking);
  }

  @Override
  @Transactional
  public StockPickingResponse confirmPicking(UUID organizationId, UUID pickingId) {
    log.info("Confirming picking {}", pickingId);
    StockPicking picking = findPickingByIdAndVerifyOrganization(pickingId, organizationId);
    
    if (picking.getState() != StockPickingState.DRAFT) {
      throw new BadRequestException("Picking is not in DRAFT state.");
    }

    picking.setState(StockPickingState.CONFIRMED);
    for (StockMove move : picking.getStockMoves()) {
      move.setState(StockMoveState.CONFIRMED);
    }

    picking = stockPickingRepository.save(picking);
    return stockPickingMapper.toResponse(picking);
  }

  @Override
  @Transactional
  public StockPickingResponse assignPicking(UUID organizationId, UUID pickingId) {
    log.info("Assigning/reserving stock for picking {}", pickingId);
    StockPicking picking = findPickingByIdAndVerifyOrganization(pickingId, organizationId);

    if (picking.getState() != StockPickingState.CONFIRMED) {
      throw new BadRequestException("Picking must be in CONFIRMED state to reserve stock.");
    }

    if (picking.getPickingType() == PickingType.OUTGOING) {
      // Perform reservation
      for (StockMove move : picking.getStockMoves()) {
        reservationService.reserveStock(
            organizationId,
            move.getProduct().getId(),
            picking.getLocation().getWarehouse().getId(),
            move.getLocation().getId(),
            move.getLot() != null ? move.getLot().getId() : null,
            move.getProductUomQty()
        );
      }
    }

    picking.setState(StockPickingState.ASSIGNED);
    for (StockMove move : picking.getStockMoves()) {
      move.setState(StockMoveState.ASSIGNED);
    }

    picking = stockPickingRepository.save(picking);
    return stockPickingMapper.toResponse(picking);
  }

  @Override
  @Transactional
  public StockPickingResponse completePicking(UUID organizationId, UUID pickingId) {
    log.info("Completing picking {}", pickingId);
    StockPicking picking = findPickingByIdAndVerifyOrganization(pickingId, organizationId);

    if (picking.getState() == StockPickingState.DONE || picking.getState() == StockPickingState.CANCEL) {
      throw new BadRequestException("Picking is already finalized.");
    }

    // Outgoing pickings should ideally be ASSIGNED first, but we can accept CONFIRMED too
    if (picking.getPickingType() == PickingType.OUTGOING && picking.getState() != StockPickingState.ASSIGNED) {
      // Auto-assign if possible
      assignPicking(organizationId, pickingId);
    }

    // Execute physical stock movement
    for (StockMove move : picking.getStockMoves()) {
      UUID productId = move.getProduct().getId();
      UUID srcLocId = move.getLocation().getId();
      UUID destLocId = move.getLocationDest().getId();
      UUID lotId = move.getLot() != null ? move.getLot().getId() : null;
      BigDecimal qty = move.getProductUomQty();

      if (picking.getPickingType() == PickingType.OUTGOING) {
        // Release reservation and transfer
        stockQuantService.addStock(organizationId, productId, srcLocId, lotId, qty.negate());
        stockQuantService.releaseReservedStock(organizationId, productId, srcLocId, lotId, qty);
        stockQuantService.addStock(organizationId, productId, destLocId, lotId, qty);
      } else {
        // INCOMING or INTERNAL
        stockQuantService.addStock(organizationId, productId, srcLocId, lotId, qty.negate());
        stockQuantService.addStock(organizationId, productId, destLocId, lotId, qty);
      }

      move.setQuantityDone(qty);
      move.setState(StockMoveState.DONE);
    }

    picking.setState(StockPickingState.DONE);
    picking.setDateDone(Instant.now());

    // Placeholder for COGS Engine valuation trigger (Phase 4)
    triggerValuation(picking);

    picking = stockPickingRepository.save(picking);
    return stockPickingMapper.toResponse(picking);
  }

  private void triggerValuation(StockPicking picking) {
    try {
      log.info("Triggering valuation for picking {}", picking.getName());
      for (StockMove move : picking.getStockMoves()) {
        CostMethod method = CostMethod.FIFO;
        if (move.getProduct().getProductTemplate() != null) {
          method = move.getProduct().getProductTemplate().getValuationMethod();
        }
        stockValuationService.calculateCOGS(move, method);
      }
    } catch (Exception e) {
      log.error("Failed to process valuation for picking: {}", picking.getId(), e);
    }
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

  private StockPicking findPickingByIdAndVerifyOrganization(UUID pickingId, UUID organizationId) {
    StockPicking picking = stockPickingRepository.findById(pickingId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock picking not found: " + pickingId));
    if (!picking.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Stock picking does not belong to the specified organization.");
    }
    return picking;
  }
}
