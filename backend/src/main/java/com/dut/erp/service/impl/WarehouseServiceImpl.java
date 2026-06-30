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
import com.dut.erp.entity.Product;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.WarehouseMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.service.SecurityAuthService;
import com.dut.erp.service.WarehouseService;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.util.SecurityUtils;
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
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.OrderRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

  private final OrganizationRepository organizationRepository;
  private final WarehouseRepository warehouseRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final OrderRepository orderRepository;
  private final PermissionRepository permissionRepository;
  private final WarehouseMapper warehouseMapper;
  private final SecurityAuthService securityAuthService;

  @Override
  public PagedEntityResponse<WarehouseBaseResponse> getWarehouses(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching warehouses for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
    boolean isAllWarehouses = securityAuthService.isAdmin(currentUser) || 
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            currentUser.getId(), organizationId, "warehouses:read_all"
        );

    Page<UUID> ids;
    if (isAllWarehouses) {
      ids = warehouseRepository.findIdsByOrganizationId(organizationId, pageable);
    } else {
      ids = warehouseRepository.findIdsByOrganizationIdAndUserId(organizationId, currentUser.getId(), pageable);
    }

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
    securityAuthService.isWarehouseStaffOrManagerOrAdmin(warehouse, SecurityUtils.getCurrentUser());
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
            .isActive(Boolean.TRUE)
            .staff(staff)
            .manager(manager)
            .build();

    warehouse = warehouseRepository.save(warehouse);
    log.info("Created warehouse {} in organization {}", warehouse.getId(), organizationId);

    // Seed a zero-quantity InventoryBalance for every product in the organization
    List<Product> products = productRepository.findAllByOrganizationId(organizationId);
    if (!products.isEmpty()) {
      final Warehouse savedWarehouse = warehouse;
      List<InventoryBalance> balances = products.stream()
          .map(product -> InventoryBalance.builder()
              .warehouse(savedWarehouse)
              .product(product)
              .build())
          .collect(Collectors.toList());
      inventoryBalanceRepository.saveAll(balances);
      log.info("Seeded {} inventory balance(s) for warehouse {} across products",
          balances.size(), savedWarehouse.getId());
    }

    return warehouseMapper.toResponse(warehouse);
  }

  @Override
  @Transactional
  public WarehouseResponse updateWarehouse(
      UUID organizationId, UUID warehouseId, UpdateWarehouseRequest request) {
    log.info("Updating warehouse {} in organization {}", warehouseId, organizationId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);

    securityAuthService.isWarehouseManagerOrAdmin(warehouse, SecurityUtils.getCurrentUser());

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
    if (!securityAuthService.isAdmin(SecurityUtils.getCurrentUser())) {
      throw new org.springframework.security.access.AccessDeniedException("Access denied: Only system administrators can delete warehouses.");
    }
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

  @Override
  public com.dut.erp.dto.response.WarehouseMetricsResponse getWarehouseMetrics(UUID organizationId, UUID warehouseId) {
    log.info("Fetching metrics for warehouse {} in organization {}", warehouseId, organizationId);
    Warehouse warehouse = findWarehouseByIdAndOrganizationId(warehouseId, organizationId);
    securityAuthService.isWarehouseStaffOrManagerOrAdmin(warehouse, SecurityUtils.getCurrentUser());

    // 1. Receipts Metrics
    long receiptsToProcess = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.RECEIPT),
        List.of(com.dut.erp.enums.DocumentStatus.DRAFT)
    );
    long receiptsBackorders = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.RECEIPT),
        List.of(com.dut.erp.enums.DocumentStatus.WAITING_FOR_STOCK)
    );
    long receiptsLate = inventoryDocumentRepository.countLateDocuments(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.RECEIPT),
        List.of(com.dut.erp.enums.DocumentStatus.COMPLETED, com.dut.erp.enums.DocumentStatus.CANCELLED)
    );

    // 2. Deliveries Metrics
    long deliveriesToProcess = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.ISSUE),
        List.of(com.dut.erp.enums.DocumentStatus.DRAFT)
    );
    long deliveriesBackorders = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.ISSUE),
        List.of(com.dut.erp.enums.DocumentStatus.WAITING_FOR_STOCK)
    );
    long deliveriesLate = inventoryDocumentRepository.countLateDocuments(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.ISSUE),
        List.of(com.dut.erp.enums.DocumentStatus.COMPLETED, com.dut.erp.enums.DocumentStatus.CANCELLED)
    );

    // 3. Transfers Metrics
    long transfersToProcess = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.TRANSFER_IN, com.dut.erp.enums.DocumentType.TRANSFER_OUT),
        List.of(com.dut.erp.enums.DocumentStatus.DRAFT)
    );
    long transfersBackorders = inventoryDocumentRepository.countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.TRANSFER_IN, com.dut.erp.enums.DocumentType.TRANSFER_OUT),
        List.of(com.dut.erp.enums.DocumentStatus.WAITING_FOR_STOCK)
    );
    long transfersLate = inventoryDocumentRepository.countLateDocuments(
        warehouseId,
        List.of(com.dut.erp.enums.DocumentType.TRANSFER_IN, com.dut.erp.enums.DocumentType.TRANSFER_OUT),
        List.of(com.dut.erp.enums.DocumentStatus.COMPLETED, com.dut.erp.enums.DocumentStatus.CANCELLED)
    );

    // 4. Pending Fulfillment Count (Global for organization)
    long pendingFulfillmentCount = orderRepository.countPendingFulfillmentOrders(organizationId);

    return new com.dut.erp.dto.response.WarehouseMetricsResponse(
        new com.dut.erp.dto.response.WarehouseMetricsResponse.MetricDetail(receiptsToProcess, receiptsBackorders, receiptsLate),
        new com.dut.erp.dto.response.WarehouseMetricsResponse.MetricDetail(deliveriesToProcess, deliveriesBackorders, deliveriesLate),
        new com.dut.erp.dto.response.WarehouseMetricsResponse.MetricDetail(transfersToProcess, transfersBackorders, transfersLate),
        pendingFulfillmentCount
    );
  }
}
