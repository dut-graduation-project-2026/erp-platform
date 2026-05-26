package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateStockInventoryRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.SubmitInventoryCountRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockInventoryBaseResponse;
import com.dut.erp.dto.response.StockInventoryResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.StockInventory;
import com.dut.erp.entity.StockInventoryLine;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.enums.StockInventoryState;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockInventoryMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.StockInventoryLineRepository;
import com.dut.erp.repository.StockInventoryRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.service.StockInventoryService;
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
public class StockInventoryServiceImpl implements StockInventoryService {

  private final OrganizationRepository organizationRepository;
  private final StockLocationRepository stockLocationRepository;
  private final StockInventoryRepository stockInventoryRepository;
  private final StockInventoryLineRepository stockInventoryLineRepository;
  private final StockQuantRepository stockQuantRepository;
  private final StockQuantService stockQuantService;
  private final StockInventoryMapper stockInventoryMapper;

  @Override
  public PagedEntityResponse<StockInventoryBaseResponse> getInventories(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching stock inventories for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.desc("createdAt")));

    Page<UUID> ids = stockInventoryRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, StockInventory> inventoryMap =
        stockInventoryRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(StockInventory::getId, Function.identity()));

    List<StockInventoryBaseResponse> responses =
        ids.getContent().stream()
            .map(inventoryMap::get)
            .filter(Objects::nonNull)
            .map(stockInventoryMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public StockInventoryResponse getInventoryById(UUID organizationId, UUID inventoryId) {
    StockInventory inventory = findInventoryByIdAndVerifyOrganization(inventoryId, organizationId);
    return stockInventoryMapper.toResponse(inventory);
  }

  @Override
  @Transactional
  public StockInventoryResponse createInventory(UUID organizationId, CreateStockInventoryRequest request) {
    log.info("Creating stock inventory sheet '{}' in org {}", request.name(), organizationId);
    Organization organization = findOrganizationById(organizationId);

    StockLocation location = null;
    if (request.locationId() != null) {
      location = stockLocationRepository.findById(request.locationId())
          .orElseThrow(() -> new ResourceNotFoundException("Location not found: " + request.locationId()));
      if (!location.getWarehouse().getOrganization().getId().equals(organizationId)) {
        throw new BadRequestException("Location does not belong to the specified organization.");
      }
    }

    StockInventory inventory = StockInventory.builder()
        .organization(organization)
        .location(location)
        .name(request.name())
        .state(StockInventoryState.DRAFT)
        .inventoryDate(Instant.now())
        .inventoryLines(new ArrayList<>())
        .build();

    inventory = stockInventoryRepository.save(inventory);
    return stockInventoryMapper.toResponse(inventory);
  }

  @Override
  @Transactional
  public StockInventoryResponse startInventory(UUID organizationId, UUID inventoryId) {
    log.info("Starting stock inventory {}", inventoryId);
    StockInventory inventory = findInventoryByIdAndVerifyOrganization(inventoryId, organizationId);

    if (inventory.getState() != StockInventoryState.DRAFT) {
      throw new BadRequestException("Stock inventory is not in DRAFT state.");
    }

    inventory.setState(StockInventoryState.IN_PROGRESS);

    // Fetch system stock quants for comparison
    UUID filterLocId = inventory.getLocation() != null ? inventory.getLocation().getId() : null;
    List<StockQuant> quants = stockQuantRepository.findAll().stream()
        .filter(q -> q.getLocation().getWarehouse().getOrganization().getId().equals(organizationId))
        .filter(q -> filterLocId == null || q.getLocation().getId().equals(filterLocId))
        .collect(Collectors.toList());

    for (StockQuant quant : quants) {
      StockInventoryLine line = StockInventoryLine.builder()
          .inventory(inventory)
          .product(quant.getProduct())
          .location(quant.getLocation())
          .lot(quant.getLot())
          .theoreticalQty(quant.getQuantity())
          .checkedQty(BigDecimal.ZERO)
          .build();
      inventory.getInventoryLines().add(line);
    }

    inventory = stockInventoryRepository.save(inventory);
    return stockInventoryMapper.toResponse(inventory);
  }

  @Override
  @Transactional
  public StockInventoryResponse submitCounts(UUID organizationId, UUID inventoryId, SubmitInventoryCountRequest request) {
    log.info("Submitting counts for stock inventory {}", inventoryId);
    StockInventory inventory = findInventoryByIdAndVerifyOrganization(inventoryId, organizationId);

    if (inventory.getState() != StockInventoryState.IN_PROGRESS) {
      throw new BadRequestException("Counts can only be submitted for IN_PROGRESS inventories.");
    }

    for (SubmitInventoryCountRequest.SubmitCountLine countLine : request.countLines()) {
      StockInventoryLine line = inventory.getInventoryLines().stream()
          .filter(l -> l.getId().equals(countLine.lineId()))
          .findFirst()
          .orElseThrow(() -> new ResourceNotFoundException("Inventory line not found: " + countLine.lineId()));

      line.setCheckedQty(countLine.checkedQty());
      stockInventoryLineRepository.save(line);
    }

    inventory = stockInventoryRepository.save(inventory);
    return stockInventoryMapper.toResponse(inventory);
  }

  @Override
  @Transactional
  public StockInventoryResponse validateInventory(UUID organizationId, UUID inventoryId) {
    log.info("Validating stock inventory {}", inventoryId);
    StockInventory inventory = findInventoryByIdAndVerifyOrganization(inventoryId, organizationId);

    if (inventory.getState() != StockInventoryState.IN_PROGRESS) {
      throw new BadRequestException("Only IN_PROGRESS inventories can be validated.");
    }

    // Apply corrective stock movements
    for (StockInventoryLine line : inventory.getInventoryLines()) {
      BigDecimal variance = line.getCheckedQty().subtract(line.getTheoreticalQty());
      if (variance.compareTo(BigDecimal.ZERO) != 0) {
        log.info("Inventory adjustment: product={} location={} lot={} variance={}",
            line.getProduct().getSku(), line.getLocation().getCode(), 
            line.getLot() != null ? line.getLot().getLotNumber() : "none", variance);
            
        // Corrective quantity change
        stockQuantService.addStock(
            organizationId,
            line.getProduct().getId(),
            line.getLocation().getId(),
            line.getLot() != null ? line.getLot().getId() : null,
            variance
        );
      }
    }

    inventory.setState(StockInventoryState.DONE);
    inventory.setInventoryDate(Instant.now());
    inventory = stockInventoryRepository.save(inventory);
    return stockInventoryMapper.toResponse(inventory);
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

  private StockInventory findInventoryByIdAndVerifyOrganization(UUID inventoryId, UUID organizationId) {
    StockInventory inventory = stockInventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock inventory not found: " + inventoryId));
    if (!inventory.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Stock inventory does not belong to the specified organization.");
    }
    return inventory;
  }
}
