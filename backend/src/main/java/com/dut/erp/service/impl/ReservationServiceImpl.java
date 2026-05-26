package com.dut.erp.service.impl;

import com.dut.erp.entity.StockQuant;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.service.ReservationService;
import com.dut.erp.service.StockQuantService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

  private final StockQuantRepository stockQuantRepository;
  private final StockQuantService stockQuantService;

  @Override
  public void reserveStock(
      UUID organizationId,
      UUID productId,
      UUID warehouseId,
      UUID locationId,
      UUID lotId,
      BigDecimal quantity
  ) {
    log.info("Request to reserve {} units of product {} in org {}", quantity, productId, organizationId);
    
    if (locationId != null) {
      stockQuantService.reserveStock(organizationId, productId, locationId, lotId, quantity);
      return;
    }

    // Auto-allocate reservation across available quants
    List<StockQuant> quants = stockQuantRepository.findAvailableQuantsByProduct(productId).stream()
        .filter(q -> warehouseId == null || q.getLocation().getWarehouse().getId().equals(warehouseId))
        .filter(q -> lotId == null || (q.getLot() != null && q.getLot().getId().equals(lotId)))
        .sorted((q1, q2) -> {
          BigDecimal avail1 = q1.getQuantity().subtract(q1.getReservedQuantity());
          BigDecimal avail2 = q2.getQuantity().subtract(q2.getReservedQuantity());
          return avail2.compareTo(avail1); // descending order of available quantity
        })
        .collect(Collectors.toList());

    BigDecimal remaining = quantity;
    for (StockQuant quant : quants) {
      BigDecimal available = quant.getQuantity().subtract(quant.getReservedQuantity());
      BigDecimal reserveAmount = available.min(remaining);
      
      stockQuantService.reserveStock(
          organizationId, 
          productId, 
          quant.getLocation().getId(), 
          quant.getLot() != null ? quant.getLot().getId() : null, 
          reserveAmount
      );
      
      remaining = remaining.subtract(reserveAmount);
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
    }

    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
      throw new BadRequestException("Insufficient available stock to reserve. Shortfall: " + remaining);
    }
  }

  @Override
  public void releaseReservation(
      UUID organizationId,
      UUID productId,
      UUID warehouseId,
      UUID locationId,
      UUID lotId,
      BigDecimal quantity
  ) {
    log.info("Request to release {} reserved units of product {} in org {}", quantity, productId, organizationId);

    if (locationId != null) {
      stockQuantService.releaseReservedStock(organizationId, productId, locationId, lotId, quantity);
      return;
    }

    List<StockQuant> quants = stockQuantRepository.findAllByProductId(productId).stream()
        .filter(q -> q.getReservedQuantity().compareTo(BigDecimal.ZERO) > 0)
        .filter(q -> warehouseId == null || q.getLocation().getWarehouse().getId().equals(warehouseId))
        .filter(q -> lotId == null || (q.getLot() != null && q.getLot().getId().equals(lotId)))
        .sorted(Comparator.comparing(StockQuant::getReservedQuantity).reversed()) // release from largest reserved locations first
        .collect(Collectors.toList());

    BigDecimal remaining = quantity;
    for (StockQuant quant : quants) {
      BigDecimal reserved = quant.getReservedQuantity();
      BigDecimal releaseAmount = reserved.min(remaining);

      stockQuantService.releaseReservedStock(
          organizationId,
          productId,
          quant.getLocation().getId(),
          quant.getLot() != null ? quant.getLot().getId() : null,
          releaseAmount
      );

      remaining = remaining.subtract(releaseAmount);
      if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
    }

    if (remaining.compareTo(BigDecimal.ZERO) > 0) {
      throw new BadRequestException("Could not release " + quantity + " reserved units; only released " 
          + quantity.subtract(remaining));
    }
  }
}
