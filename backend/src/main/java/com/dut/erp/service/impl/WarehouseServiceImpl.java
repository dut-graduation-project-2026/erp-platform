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
import com.dut.erp.entity.User;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.WarehouseMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.WarehouseService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
  private final UserRepository userRepository;
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

    // Resolve staff — all must belong to the organization
    List<User> staff = resolveStaff(request.staffIds(), organizationId);

    // Validate: manager must be in the staff list
    UUID managerId = request.managerId();
    validateManagerInStaff(managerId, request.staffIds());

    User manager = staff.stream()
        .filter(u -> u.getId().equals(managerId))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Manager resolved but not found in staff list"));

    Warehouse warehouse =
        Warehouse.builder()
            .organization(organization)
            .name(request.name())
            .code(request.code())
            .address(request.address())
            .description(request.description())
            .maximumCapacity(request.maximumCapacity())
            .isActive(Boolean.TRUE)
            .staff(staff)
            .manager(manager)
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

    // Resolve staff — all must belong to the organization
    List<User> staff = resolveStaff(request.staffIds(), organizationId);

    // Validate: manager must be in the staff list
    UUID managerId = request.managerId();
    validateManagerInStaff(managerId, request.staffIds());

    User manager = staff.stream()
        .filter(u -> u.getId().equals(managerId))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Manager resolved but not found in staff list"));

    if (request.isActive() != null) {
      warehouse.setIsActive(request.isActive());
    }
    warehouse.setName(request.name());
    warehouse.setCode(request.code());
    warehouse.setAddress(request.address());
    warehouse.setDescription(request.description());
    warehouse.setMaximumCapacity(request.maximumCapacity());
    warehouse.setStaff(staff);
    warehouse.setManager(manager);

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

  /**
   * Resolve staff list from IDs, ensuring all users belong to the given organization.
   * Throws ResourceNotFoundException if any user is not found or not in the organization.
   */
  private List<User> resolveStaff(List<UUID> staffIds, UUID organizationId) {
    List<User> staff = userRepository.findAllByIdInAndOrganizationId(staffIds, organizationId);
    if (staff.size() != staffIds.size()) {
      Set<UUID> foundIds = staff.stream().map(User::getId).collect(Collectors.toSet());
      List<UUID> missingIds = staffIds.stream().filter(id -> !foundIds.contains(id)).toList();
      throw new ResourceNotFoundException(
          "The following user IDs are not found in this organization: " + missingIds);
    }
    return staff;
  }

  /**
   * Validates that the given managerId is included in the staffIds list.
   * Throws BadRequestException if not.
   */
  private void validateManagerInStaff(UUID managerId, List<UUID> staffIds) {
    if (!staffIds.contains(managerId)) {
      throw new BadRequestException(
          "Manager (id: " + managerId + ") must be included in the staff list.");
    }
  }
}
