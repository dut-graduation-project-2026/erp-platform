package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateStockLocationRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateStockLocationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockLocationBaseResponse;
import com.dut.erp.dto.response.StockLocationResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.StockLocationMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.StockLocationService;
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
public class StockLocationServiceImpl implements StockLocationService {

  private final OrganizationRepository organizationRepository;
  private final WarehouseRepository warehouseRepository;
  private final StockLocationRepository stockLocationRepository;
  private final StockLocationMapper stockLocationMapper;

  @Override
  public PagedEntityResponse<StockLocationBaseResponse> getLocationsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching locations for organization {}", organizationId);
    findOrganizationById(organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("code")));

    Page<UUID> ids = stockLocationRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, StockLocation> locationMap =
        stockLocationRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(StockLocation::getId, Function.identity()));

    List<StockLocationBaseResponse> responses =
        ids.getContent().stream()
            .map(locationMap::get)
            .filter(Objects::nonNull)
            .map(stockLocationMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public PagedEntityResponse<StockLocationBaseResponse> getLocationsByWarehouseId(
      UUID organizationId, UUID warehouseId, PaginationRequest paginationRequest) {
    log.info("Fetching locations for warehouse {} in organization {}", warehouseId, organizationId);
    findOrganizationById(organizationId);
    verifyWarehouseBelongsToOrganization(warehouseId, organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("code")));

    Page<UUID> ids = stockLocationRepository.findIdsByWarehouseId(warehouseId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, StockLocation> locationMap =
        stockLocationRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(StockLocation::getId, Function.identity()));

    List<StockLocationBaseResponse> responses =
        ids.getContent().stream()
            .map(locationMap::get)
            .filter(Objects::nonNull)
            .map(stockLocationMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public StockLocationResponse getLocationById(UUID organizationId, UUID locationId) {
    log.info("Fetching location {} for organization {}", locationId, organizationId);
    findOrganizationById(organizationId);
    StockLocation location = findLocationByIdAndVerifyOrganization(locationId, organizationId);
    return stockLocationMapper.toResponse(location);
  }

  @Override
  @Transactional
  public StockLocationResponse createLocation(UUID organizationId, CreateStockLocationRequest request) {
    log.info("Creating location with code {} in warehouse {} for organization {}", 
        request.code(), request.warehouseId(), organizationId);
    findOrganizationById(organizationId);
    Warehouse warehouse = verifyWarehouseBelongsToOrganization(request.warehouseId(), organizationId);
    assertCodeAvailable(request.code(), request.warehouseId());

    StockLocation parent = null;
    if (request.parentId() != null) {
      parent = stockLocationRepository.findById(request.parentId())
          .orElseThrow(() -> new ResourceNotFoundException("Parent location not found with id: " + request.parentId()));
      if (!parent.getWarehouse().getId().equals(warehouse.getId())) {
        throw new BadRequestException("Parent location must belong to the same warehouse.");
      }
    }

    StockLocation location =
        StockLocation.builder()
            .warehouse(warehouse)
            .parent(parent)
            .name(request.name())
            .code(request.code())
            .locationType(request.locationType())
            .isActive(true)
            .build();

    location = stockLocationRepository.save(location);
    log.info("Created location {} in organization {}", location.getId(), organizationId);
    return stockLocationMapper.toResponse(location);
  }

  @Override
  @Transactional
  public StockLocationResponse updateLocation(
      UUID organizationId, UUID locationId, UpdateStockLocationRequest request) {
    log.info("Updating location {} in organization {}", locationId, organizationId);
    findOrganizationById(organizationId);
    StockLocation location = findLocationByIdAndVerifyOrganization(locationId, organizationId);

    location.setName(request.name());
    location.setLocationType(request.locationType());
    if (request.isActive() != null) {
      location.setIsActive(request.isActive());
    }

    location = stockLocationRepository.save(location);
    log.info("Updated location {} in organization {}", locationId, organizationId);
    return stockLocationMapper.toResponse(location);
  }

  @Override
  @Transactional
  public void deleteLocation(UUID organizationId, UUID locationId) {
    log.info("Deleting location {} from organization {}", locationId, organizationId);
    findOrganizationById(organizationId);
    StockLocation location = findLocationByIdAndVerifyOrganization(locationId, organizationId);
    stockLocationRepository.delete(location);
    log.info("Deleted location {} from organization {}", locationId, organizationId);
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

  private Warehouse verifyWarehouseBelongsToOrganization(UUID warehouseId, UUID organizationId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
    if (!warehouse.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Warehouse does not belong to the specified organization.");
    }
    return warehouse;
  }

  private StockLocation findLocationByIdAndVerifyOrganization(UUID locationId, UUID organizationId) {
    StockLocation location = stockLocationRepository.findById(locationId)
        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));
    if (!location.getWarehouse().getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Location does not belong to the specified organization.");
    }
    return location;
  }

  private void assertCodeAvailable(String code, UUID warehouseId) {
    if (stockLocationRepository.findByCodeAndWarehouseId(code, warehouseId).isPresent()) {
      throw new ResourceAlreadyExistsException(
          "Location with code '" + code + "' already exists in this warehouse.");
    }
  }
}
