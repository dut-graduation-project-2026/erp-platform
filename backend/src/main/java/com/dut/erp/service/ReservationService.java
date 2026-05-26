package com.dut.erp.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReservationService {

  void reserveStock(UUID organizationId, UUID productId, UUID warehouseId, UUID locationId, UUID lotId, BigDecimal quantity);

  void releaseReservation(UUID organizationId, UUID productId, UUID warehouseId, UUID locationId, UUID lotId, BigDecimal quantity);
}
