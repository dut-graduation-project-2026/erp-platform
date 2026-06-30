package com.dut.erp.repository;

import com.dut.erp.entity.InventoryDocumentLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryDocumentLineRepository extends JpaRepository<InventoryDocumentLine, UUID> {
  List<InventoryDocumentLine> findAllByInventoryDocumentId(UUID inventoryDocumentId);

  @Query("""
      SELECT COALESCE(SUM(line.valuation), 0)
      FROM InventoryDocumentLine line
      JOIN line.inventoryDocument doc
      JOIN doc.warehouse w
      WHERE w.organization.id = :orgId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.RECEIPT, com.dut.erp.enums.DocumentType.TRANSFER_IN)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity > 0))
        AND doc.dateDone >= :startDate AND doc.dateDone <= :endDate
      """)
  BigDecimal sumInboundValuationByOrgAndDateRange(
      @Param("orgId") UUID orgId,
      @Param("startDate") java.time.Instant startDate,
      @Param("endDate") java.time.Instant endDate);

  @Query("""
      SELECT COALESCE(SUM(line.valuation), 0)
      FROM InventoryDocumentLine line
      JOIN line.inventoryDocument doc
      JOIN doc.warehouse w
      WHERE w.organization.id = :orgId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.ISSUE, com.dut.erp.enums.DocumentType.TRANSFER_OUT)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity < 0))
        AND doc.dateDone >= :startDate AND doc.dateDone <= :endDate
      """)
  BigDecimal sumOutboundValuationByOrgAndDateRange(
      @Param("orgId") UUID orgId,
      @Param("startDate") java.time.Instant startDate,
      @Param("endDate") java.time.Instant endDate);

  @Query("""
      SELECT line FROM InventoryDocumentLine line
      JOIN FETCH line.inventoryDocument doc
      WHERE line.product.id = :productId
        AND doc.warehouse.id = :warehouseId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.RECEIPT, com.dut.erp.enums.DocumentType.TRANSFER_IN)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity > 0))
        AND line.remainingQuantity > 0
      ORDER BY doc.dateDone ASC, doc.createdAt ASC
      """)
  List<InventoryDocumentLine> findAvailableInboundLayersFifo(
      @Param("productId") UUID productId, 
      @Param("warehouseId") UUID warehouseId);

  @Query("""
      SELECT line FROM InventoryDocumentLine line
      JOIN FETCH line.inventoryDocument doc
      WHERE line.product.id = :productId
        AND doc.warehouse.id = :warehouseId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.RECEIPT, com.dut.erp.enums.DocumentType.TRANSFER_IN)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity > 0))
        AND line.remainingQuantity > 0
      ORDER BY doc.dateDone DESC, doc.createdAt DESC
      """)
  List<InventoryDocumentLine> findAvailableInboundLayersLifo(
      @Param("productId") UUID productId, 
      @Param("warehouseId") UUID warehouseId);
}
