package com.dut.erp.service.impl;

import com.dut.erp.dto.request.LandedCostRequest;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockPicking;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockPickingRepository;
import com.dut.erp.service.LandedCostsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LandedCostsServiceImpl implements LandedCostsService {

  private final StockPickingRepository stockPickingRepository;
  private final ProductRepository productRepository;

  @Override
  public void allocateLandedCosts(UUID organizationId, UUID pickingId, LandedCostRequest request) {
    log.info("Allocating landed cost of {} using method {} to picking {}", 
        request.amount(), request.allocationMethod(), pickingId);

    StockPicking picking = stockPickingRepository.findByIdAndOrganizationId(pickingId, organizationId)
        .orElseThrow(() -> new ResourceNotFoundException("Stock picking not found: " + pickingId));

    List<StockMove> moves = picking.getStockMoves();
    if (moves.isEmpty()) {
      throw new BadRequestException("Picking has no stock moves to allocate costs to.");
    }

    BigDecimal totalCost = request.amount();
    String method = request.allocationMethod();

    if ("EQUAL".equalsIgnoreCase(method)) {
      BigDecimal perLineCost = totalCost.divide(BigDecimal.valueOf(moves.size()), 4, RoundingMode.HALF_UP);
      for (StockMove move : moves) {
        Product product = move.getProduct();
        BigDecimal qty = move.getProductUomQty();
        if (qty.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal costPerUnit = perLineCost.divide(qty, 4, RoundingMode.HALF_UP);
          product.setCost(product.getCost().add(costPerUnit));
          productRepository.save(product);
          log.info("Allocated equal share to product {}: +{} per unit", product.getSku(), costPerUnit);
        }
      }
    } else if ("BY_QUANTITY".equalsIgnoreCase(method)) {
      BigDecimal totalQty = moves.stream()
          .map(StockMove::getProductUomQty)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (totalQty.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Total picking quantity must be positive to allocate by quantity.");
      }

      BigDecimal costPerUnit = totalCost.divide(totalQty, 4, RoundingMode.HALF_UP);
      for (StockMove move : moves) {
        Product product = move.getProduct();
        product.setCost(product.getCost().add(costPerUnit));
        productRepository.save(product);
        log.info("Allocated quantity share to product {}: +{} per unit", product.getSku(), costPerUnit);
      }
    } else {
      throw new BadRequestException("Unknown allocation method: " + method);
    }
  }
}
