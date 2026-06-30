package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryBalanceBaseResponse;
import com.dut.erp.dto.response.InventoryBalanceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.InventoryBalanceMapper;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.InventoryBalanceService;
import com.dut.erp.dto.response.StockLayerResponse;
import com.dut.erp.repository.InventoryDocumentLineRepository;
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
public class InventoryBalanceServiceImpl implements InventoryBalanceService {

  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final WarehouseRepository warehouseRepository;
  private final InventoryBalanceMapper inventoryBalanceMapper;
  private final InventoryDocumentLineRepository inventoryDocumentLineRepository;

  @Override
  public PagedEntityResponse<InventoryBalanceBaseResponse> getBalancesByWarehouse(
      UUID organizationId, UUID warehouseId, String search, PaginationRequest paginationRequest) {

    log.info(
        "Fetching inventory balances for warehouse {} in organization {}",
        warehouseId,
        organizationId);

    // Verify warehouse belongs to the organization
    validateWarehouseBelongsToOrg(warehouseId, organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("product.name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? inventoryBalanceRepository.findIdsByWarehouseIdAndSearch(
                warehouseId, search, pageable)
            : inventoryBalanceRepository.findIdsByWarehouseId(warehouseId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, InventoryBalance> balanceMap =
        inventoryBalanceRepository.findAllByIdsWithProduct(ids.getContent()).stream()
            .collect(Collectors.toMap(InventoryBalance::getId, Function.identity()));

    List<InventoryBalanceBaseResponse> responses =
        ids.getContent().stream()
            .map(balanceMap::get)
            .filter(Objects::nonNull)
            .map(inventoryBalanceMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public InventoryBalanceResponse getBalanceById(
      UUID organizationId, UUID warehouseId, UUID balanceId) {

    log.info(
        "Fetching inventory balance {} for warehouse {} in organization {}",
        balanceId,
        warehouseId,
        organizationId);

    // Verify warehouse belongs to the organization
    validateWarehouseBelongsToOrg(warehouseId, organizationId);

    InventoryBalance balance =
        inventoryBalanceRepository
            .findByIdAndWarehouseId(balanceId, warehouseId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Inventory balance not found with id: " + balanceId));

    return inventoryBalanceMapper.toResponse(balance);
  }

  @Override
  public List<StockLayerResponse> getActiveLayers(UUID organizationId, UUID warehouseId, UUID productId) {
    log.info("Fetching active stock layers for product {} in warehouse {} of organization {}", 
        productId, warehouseId, organizationId);
    
    validateWarehouseBelongsToOrg(warehouseId, organizationId);

    return inventoryDocumentLineRepository.findAvailableInboundLayersFifo(productId, warehouseId)
        .stream()
        .map(line -> new StockLayerResponse(
            line.getId(),
            line.getInventoryDocument().getName(),
            line.getInventoryDocument().getDateDone(),
            line.getQuantity(),
            line.getRemainingQuantity(),
            line.getUnitCost()
        ))
        .collect(Collectors.toList());
  }

  // ---- Private helpers ----

  private void validateWarehouseBelongsToOrg(UUID warehouseId, UUID organizationId) {
    if (!warehouseRepository.existsByOrganizationIdAndIdInternal(organizationId, warehouseId)) {
      throw new ResourceNotFoundException(
          "Warehouse " + warehouseId + " does not belong to organization " + organizationId);
    }
  }
}
