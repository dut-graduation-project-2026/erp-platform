package com.dut.erp.service.impl;

import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.PickingOptimalService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickingOptimalServiceImpl implements PickingOptimalService {

  private final InventoryBalanceRepository inventoryBalanceRepository;

  @Override
  public Warehouse recommendOptimalWarehouse(
      UUID organizationId, UUID productId, BigDecimal quantity, UUID currentWarehouseId) {
    log.info(
        "Recommending optimal warehouse for product {} (req. qty: {}) in org {}",
        productId,
        quantity,
        organizationId);

    List<InventoryBalance> balances =
        inventoryBalanceRepository.findAllByProductIdAndOrganizationId(productId, organizationId);

    return balances.stream()
        .filter(ib -> ib.getQuantity().compareTo(quantity) >= 0)
        .max(Comparator.comparing(InventoryBalance::getQuantity))
        .map(InventoryBalance::getWarehouse)
        .orElseGet(
            () -> {
              // If no other warehouse has enough stock, fallback to the warehouse with the highest
              // stock
              return balances.stream()
                  .max(Comparator.comparing(InventoryBalance::getQuantity))
                  .map(InventoryBalance::getWarehouse)
                  .orElse(null);
            });
  }
}
