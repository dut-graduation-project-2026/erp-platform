package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateWarehouseRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateWarehouseRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.WarehouseBaseResponse;
import com.dut.erp.dto.response.WarehouseResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.WarehouseMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.WarehouseService;
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
public class WarehouseServiceImpl implements WarehouseService {

  private final OrganizationRepository organizationRepository;
  private final WarehouseRepository warehouseRepository;
  private final WarehouseMapper warehouseMapper;

  @Override
  public PagedEntityResponse<WarehouseBaseResponse> getWarehouses(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching warehouses for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids = warehouseRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Warehouse> warehouseMap =
        warehouseRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Warehouse::getId, Function.identity()));

    List<WarehouseBaseResponse> responses =
        ids.getContent().stream()
            .map(warehouseMap::get)
            .filter(Objects::nonNull)
            .map(warehouseMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public WarehouseResponse getWarehouseById(UUID organizationId, UUID warehouseId) {
    log.info("Fetching warehouse {} for organization {}", warehouseId, organizationId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    return warehouseMapper.toResponse(warehouse);
  }

  @Override
  @Transactional
  public WarehouseResponse createWarehouse(UUID organizationId, CreateWarehouseRequest request) {
    log.info("Creating warehouse in organization {}", organizationId);
    Organization organization = findOrganizationById(organizationId);

    if (warehouseRepository.existsByOrganizationIdAndCode(organizationId, request.code())) {
      throw new ResourceAlreadyExistsException(
          "Warehouse with code " + request.code() + " already exists in this organization.");
    }

    Warehouse warehouse =
        Warehouse.builder()
            .organization(organization)
            .name(request.name())
            .code(request.code())
            .address(request.address())
            .isActive(Boolean.TRUE)
            .build();

    warehouse = warehouseRepository.save(warehouse);
    log.info("Created warehouse {} in organization {}", warehouse.getId(), organizationId);
    return warehouseMapper.toResponse(warehouse);
  }

  @Override
  @Transactional
  public WarehouseResponse updateWarehouse(
      UUID organizationId, UUID warehouseId, UpdateWarehouseRequest request) {
    log.info("Updating warehouse {} in organization {}", warehouseId, organizationId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    if (warehouseRepository.existsByOrganizationIdAndCodeAndIdNot(organizationId, request.code(), warehouseId)) {
      throw new ResourceAlreadyExistsException(
          "Warehouse with code " + request.code() + " already exists in this organization.");
    }

    if (request.isActive() != null) {
      warehouse.setIsActive(request.isActive());
    }
    warehouse.setName(request.name());
    warehouse.setCode(request.code());
    warehouse.setAddress(request.address());

    warehouse = warehouseRepository.save(warehouse);
    log.info("Updated warehouse {} in organization {}", warehouseId, organizationId);
    return warehouseMapper.toResponse(warehouse);
  }

  @Override
  @Transactional
  public void deleteWarehouse(UUID organizationId, UUID warehouseId) {
    log.info("Deleting warehouse {} in organization {}", warehouseId, organizationId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    warehouseRepository.delete(warehouse);
    log.info("Deleted warehouse {} from organization {}", warehouseId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Warehouse findWarehouseByIdAndOrganizationId(UUID warehouseId, UUID organizationId) {
    return warehouseRepository
        .findByIdAndOrganizationId(warehouseId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
  }
}
