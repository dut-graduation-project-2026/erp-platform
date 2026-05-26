package com.dut.erp.service.impl;

import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.enums.CostMethod;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.StockValuationService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
public class StockValuationServiceImpl implements StockValuationService {

  private final ProductRepository productRepository;
  private final StockValuationRepository stockValuationRepository;

  @Override
  public BigDecimal calculateCOGS(StockMove move, CostMethod method) {
    log.info("Calculating COGS for move {} using method {}", move.getId(), method);

    boolean isInbound = move.getPicking().getPickingType() == com.dut.erp.enums.PickingType.INCOMING;
    boolean isOutbound = move.getPicking().getPickingType() == com.dut.erp.enums.PickingType.OUTGOING;

    if (isInbound) {
      return processInboundValuation(move, method);
    } else if (isOutbound) {
      return processOutboundValuation(move, method);
    } else {
      return BigDecimal.ZERO;
    }
  }

  private BigDecimal processInboundValuation(StockMove move, CostMethod method) {
    Product product = move.getProduct();
    BigDecimal qty = move.getProductUomQty();
    BigDecimal unitValue = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
    BigDecimal totalValue = qty.multiply(unitValue);

    if (method == CostMethod.AVERAGE) {
      List<StockValuation> activeLayers = stockValuationRepository
          .findAllByProductIdAndRemainingQtyGreaterThanOrderByCreatedAtAsc(product.getId(), BigDecimal.ZERO);

      BigDecimal currentQty = BigDecimal.ZERO;
      BigDecimal currentValue = BigDecimal.ZERO;
      for (StockValuation layer : activeLayers) {
        currentQty = currentQty.add(layer.getRemainingQty());
        currentValue = currentValue.add(layer.getRemainingQty().multiply(layer.getUnitValue()));
      }

      BigDecimal newQty = currentQty.add(qty);
      BigDecimal newValue = currentValue.add(totalValue);

      if (newQty.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal newAverageCost = newValue.divide(newQty, 2, RoundingMode.HALF_UP);
        product.setCost(newAverageCost);
        productRepository.save(product);
        log.info("Product {} average cost updated to {}", product.getSku(), newAverageCost);
        unitValue = newAverageCost;
        totalValue = qty.multiply(unitValue);
      }
    }

    StockValuation valuation = StockValuation.builder()
        .product(product)
        .move(move)
        .quantity(qty)
        .unitValue(unitValue)
        .totalValue(totalValue)
        .remainingQty(qty)
        .remainingValue(totalValue)
        .method(method)
        .createdAt(Instant.now())
        .build();

    stockValuationRepository.save(valuation);
    log.info("Created inbound valuation layer for product {}: qty={}, value={}", product.getSku(), qty, totalValue);
    return BigDecimal.ZERO;
  }

  private BigDecimal processOutboundValuation(StockMove move, CostMethod method) {
    Product product = move.getProduct();
    BigDecimal qtyToConsume = move.getProductUomQty();
    BigDecimal totalCogs = BigDecimal.ZERO;

    List<StockValuation> layers;
    if (method == CostMethod.LIFO) {
      layers = stockValuationRepository
          .findAllByProductIdAndRemainingQtyGreaterThanOrderByCreatedAtDesc(product.getId(), BigDecimal.ZERO);
    } else {
      layers = stockValuationRepository
          .findAllByProductIdAndRemainingQtyGreaterThanOrderByCreatedAtAsc(product.getId(), BigDecimal.ZERO);
    }

    BigDecimal remainingToConsume = qtyToConsume;
    for (StockValuation layer : layers) {
      if (remainingToConsume.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }

      BigDecimal layerRemainingQty = layer.getRemainingQty();
      BigDecimal consumeQty = layerRemainingQty.min(remainingToConsume);
      BigDecimal consumedValue;

      if (method == CostMethod.AVERAGE) {
        BigDecimal avgCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
        consumedValue = consumeQty.multiply(avgCost);
      } else {
        consumedValue = consumeQty.multiply(layer.getUnitValue());
      }

      totalCogs = totalCogs.add(consumedValue);
      layer.setRemainingQty(layerRemainingQty.subtract(consumeQty));
      layer.setRemainingValue(layer.getRemainingValue().subtract(consumedValue));
      stockValuationRepository.save(layer);

      remainingToConsume = remainingToConsume.subtract(consumeQty);
    }

    BigDecimal unitCogs = qtyToConsume.compareTo(BigDecimal.ZERO) > 0
        ? totalCogs.divide(qtyToConsume, 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    StockValuation valuation = StockValuation.builder()
        .product(product)
        .move(move)
        .quantity(qtyToConsume.negate())
        .unitValue(unitCogs)
        .totalValue(totalCogs.negate())
        .remainingQty(BigDecimal.ZERO)
        .remainingValue(BigDecimal.ZERO)
        .method(method)
        .createdAt(Instant.now())
        .build();

    stockValuationRepository.save(valuation);
    log.info("Created outbound valuation layer (COGS) for product {}: qty=-{}, cogs={}", product.getSku(), qtyToConsume, totalCogs);

    return totalCogs;
  }

  @Override
  @Transactional(readOnly = true)
  public BigDecimal getCogsForSaleOrder(UUID organizationId, UUID saleOrderId) {
    log.info("Calculating total COGS for sale order {} in organization {}", saleOrderId, organizationId);
    List<StockValuation> valuations = stockValuationRepository.findAll().stream()
        .filter(v -> v.getMove().getPicking().getSaleOrder() != null 
            && v.getMove().getPicking().getSaleOrder().getId().equals(saleOrderId)
            && v.getQuantity().compareTo(BigDecimal.ZERO) < 0)
        .toList();

    BigDecimal totalCogs = BigDecimal.ZERO;
    for (StockValuation val : valuations) {
      totalCogs = totalCogs.add(val.getTotalValue().abs());
    }
    return totalCogs;
  }
}
