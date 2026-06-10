package com.dut.erp.service.impl;

import com.dut.erp.entity.Order;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.service.InventoryDocumentService;
import com.dut.erp.service.SalesOrderIntegrationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderIntegrationServiceImpl implements SalesOrderIntegrationService {

  private final InventoryDocumentService inventoryDocumentService;

  @Override
  @Transactional
  public void handleOrderConfirmation(Order order, UUID warehouseId) {
    if (warehouseId == null) {
      throw new BadRequestException("Warehouse ID must be specified when confirming the sales order");
    }

    log.info("SalesOrderIntegration: Automatically creating warehouse issue document for order {} (number: {}) in warehouse {}", 
        order.getId(), order.getOrderNumber(), warehouseId);

    inventoryDocumentService.createIssueDocumentFromOrder(
        order.getOrganization().getId(), 
        warehouseId, 
        order.getId()
    );
  }
}
