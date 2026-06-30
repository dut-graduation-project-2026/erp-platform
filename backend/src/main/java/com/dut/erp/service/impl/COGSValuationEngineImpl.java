package com.dut.erp.service.impl;

import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.InventoryDocumentLine;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.enums.CogsMethod;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.repository.InventoryDocumentLineRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.COGSValuationEngine;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class COGSValuationEngineImpl implements COGSValuationEngine {

  private final InventoryDocumentLineRepository inventoryDocumentLineRepository;
  private final StockValuationRepository stockValuationRepository;

  @Override
  @Transactional
  public void calculateCOGS(InventoryDocument document) {
    log.info("Calculating COGS for inventory document {} of type {}", document.getId(), document.getDocumentType());

    // For inbound documents (RECEIPT, TRANSFER_IN, positive ADJUSTMENT), set remainingQuantity = quantity.
    if (document.getDocumentType() == DocumentType.RECEIPT || document.getDocumentType() == DocumentType.TRANSFER_IN) {
      for (InventoryDocumentLine line : document.getLines()) {
        line.setRemainingQuantity(line.getQuantity());
      }
      inventoryDocumentLineRepository.saveAll(document.getLines());
      return;
    }

    if (document.getDocumentType() == DocumentType.ADJUSTMENT) {
      for (InventoryDocumentLine line : document.getLines()) {
        if (line.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
          line.setRemainingQuantity(line.getQuantity());
        }
      }
      inventoryDocumentLineRepository.saveAll(document.getLines());
      // Adjustments can have both positive and negative lines. We fall through to process negative lines below.
    }

    // Only valuate outgoing movements (ISSUE, TRANSFER_OUT, and negative ADJUSTMENTS)
    if (document.getDocumentType() != DocumentType.ISSUE &&
        document.getDocumentType() != DocumentType.TRANSFER_OUT &&
        document.getDocumentType() != DocumentType.ADJUSTMENT) {
      return;
    }

    UUID warehouseId = document.getWarehouse().getId();
    List<StockValuation> valuations = new ArrayList<>();
    List<InventoryDocumentLine> updatedLines = new ArrayList<>();

    for (InventoryDocumentLine line : document.getLines()) {
      BigDecimal qtyToValuate = line.getQuantity();
      
      // For ADJUSTMENT, only valuate negative adjustments (outgoing stock)
      if (document.getDocumentType() == DocumentType.ADJUSTMENT) {
        if (qtyToValuate.compareTo(BigDecimal.ZERO) >= 0) {
          continue;
        } else {
          // For negative adjustment, valuate the absolute value
          qtyToValuate = qtyToValuate.negate();
        }
      }

      Product product = line.getProduct();
      CogsMethod method = product.getCogsMethod() != null ? product.getCogsMethod() : CogsMethod.FIFO;
      log.info("Valuating product {} using method {} (qty: {})", product.getName(), method, qtyToValuate);

      if (qtyToValuate.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }

      if (method == CogsMethod.AVERAGE) {
        // WEIGHTED AVERAGE Calculation
        List<InventoryDocumentLine> layers = inventoryDocumentLineRepository
            .findAvailableInboundLayersFifo(product.getId(), warehouseId);

        BigDecimal totalValuation = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;

        for (InventoryDocumentLine layer : layers) {
          totalValuation = totalValuation.add(layer.getRemainingQuantity().multiply(layer.getUnitCost()));
          totalQty = totalQty.add(layer.getRemainingQuantity());
        }

        BigDecimal unitCost = product.getPurchasePrice(); // Fallback to product purchase price
        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
          unitCost = totalValuation.divide(totalQty, 4, RoundingMode.HALF_UP);
        }

        // Create valuation log
        valuations.add(StockValuation.builder()
            .inventoryDocumentLine(line)
            .product(product)
            .quantity(qtyToValuate)
            .unitCost(unitCost)
            .totalValuation(qtyToValuate.multiply(unitCost))
            .method(method)
            .build());

        // Update line's unit cost and valuation
        line.setUnitCost(unitCost);
        line.setValuation(qtyToValuate.multiply(unitCost));
        updatedLines.add(line);

        // Deduct from inbound layers (oldest first)
        BigDecimal remainingToDeduct = qtyToValuate;
        for (InventoryDocumentLine layer : layers) {
          if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
            break;
          }
          BigDecimal available = layer.getRemainingQuantity();
          if (available.compareTo(remainingToDeduct) >= 0) {
            layer.setRemainingQuantity(available.subtract(remainingToDeduct));
            remainingToDeduct = BigDecimal.ZERO;
          } else {
            layer.setRemainingQuantity(BigDecimal.ZERO);
            remainingToDeduct = remainingToDeduct.subtract(available);
          }
          inventoryDocumentLineRepository.save(layer);
        }
      } else {
        // FIFO or LIFO Costing
        List<InventoryDocumentLine> layers = (method == CogsMethod.LIFO)
            ? inventoryDocumentLineRepository.findAvailableInboundLayersLifo(product.getId(), warehouseId)
            : inventoryDocumentLineRepository.findAvailableInboundLayersFifo(product.getId(), warehouseId);

        BigDecimal remainingToValuate = qtyToValuate;
        BigDecimal totalLineValuation = BigDecimal.ZERO;

        for (InventoryDocumentLine layer : layers) {
          if (remainingToValuate.compareTo(BigDecimal.ZERO) <= 0) {
            break;
          }
          BigDecimal available = layer.getRemainingQuantity();
          BigDecimal quantityFromLayer = available.min(remainingToValuate);

          // Deduct from layer
          layer.setRemainingQuantity(available.subtract(quantityFromLayer));
          inventoryDocumentLineRepository.save(layer);

          // Add to valuation logs
          BigDecimal layerCost = layer.getUnitCost();
          BigDecimal layerValuation = quantityFromLayer.multiply(layerCost);
          totalLineValuation = totalLineValuation.add(layerValuation);

          valuations.add(StockValuation.builder()
              .inventoryDocumentLine(line)
              .product(product)
              .quantity(quantityFromLayer)
              .unitCost(layerCost)
              .totalValuation(layerValuation)
              .method(method)
              .build());

          remainingToValuate = remainingToValuate.subtract(quantityFromLayer);
        }

        // If not enough inbound layers, fallback to product purchase price for the remainder
        if (remainingToValuate.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal fallbackCost = product.getPurchasePrice();
          BigDecimal fallbackValuation = remainingToValuate.multiply(fallbackCost);
          totalLineValuation = totalLineValuation.add(fallbackValuation);

          valuations.add(StockValuation.builder()
              .inventoryDocumentLine(line)
              .product(product)
              .quantity(remainingToValuate)
              .unitCost(fallbackCost)
              .totalValuation(fallbackValuation)
              .method(method)
              .build());
        }

        // Set average cost on the line
        BigDecimal lineUnitCost = totalLineValuation.divide(qtyToValuate, 4, RoundingMode.HALF_UP);
        line.setUnitCost(lineUnitCost);
        line.setValuation(totalLineValuation);
        updatedLines.add(line);
      }
    }

    if (!valuations.isEmpty()) {
      stockValuationRepository.saveAll(valuations);
    }
    if (!updatedLines.isEmpty()) {
      inventoryDocumentLineRepository.saveAll(updatedLines);
    }
  }

  @Override
  public BigDecimal estimateUnitCost(Product product, UUID warehouseId, BigDecimal quantity) {
    CogsMethod method = product.getCogsMethod() != null ? product.getCogsMethod() : CogsMethod.FIFO;
    BigDecimal qtyToValuate = quantity.abs();

    if (qtyToValuate.compareTo(BigDecimal.ZERO) <= 0) {
      return product.getPurchasePrice();
    }

    if (method == CogsMethod.AVERAGE) {
      List<InventoryDocumentLine> layers = inventoryDocumentLineRepository
          .findAvailableInboundLayersFifo(product.getId(), warehouseId);

      BigDecimal totalValuation = BigDecimal.ZERO;
      BigDecimal totalQty = BigDecimal.ZERO;

      for (InventoryDocumentLine layer : layers) {
        totalValuation = totalValuation.add(layer.getRemainingQuantity().multiply(layer.getUnitCost()));
        totalQty = totalQty.add(layer.getRemainingQuantity());
      }

      if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
        return totalValuation.divide(totalQty, 4, RoundingMode.HALF_UP);
      }
      return product.getPurchasePrice();
    } else {
      List<InventoryDocumentLine> layers = (method == CogsMethod.LIFO)
          ? inventoryDocumentLineRepository.findAvailableInboundLayersLifo(product.getId(), warehouseId)
          : inventoryDocumentLineRepository.findAvailableInboundLayersFifo(product.getId(), warehouseId);

      BigDecimal remainingToValuate = qtyToValuate;
      BigDecimal totalValuation = BigDecimal.ZERO;

      for (InventoryDocumentLine layer : layers) {
        if (remainingToValuate.compareTo(BigDecimal.ZERO) <= 0) {
          break;
        }
        BigDecimal available = layer.getRemainingQuantity();
        BigDecimal quantityFromLayer = available.min(remainingToValuate);

        BigDecimal layerCost = layer.getUnitCost();
        totalValuation = totalValuation.add(quantityFromLayer.multiply(layerCost));
        remainingToValuate = remainingToValuate.subtract(quantityFromLayer);
      }

      if (remainingToValuate.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal fallbackCost = product.getPurchasePrice();
        totalValuation = totalValuation.add(remainingToValuate.multiply(fallbackCost));
      }

      return totalValuation.divide(qtyToValuate, 4, RoundingMode.HALF_UP);
    }
  }
}
