package com.dut.erp.repository;

import com.dut.erp.entity.InventoryDocumentLine;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryDocumentLineRepository extends JpaRepository<InventoryDocumentLine, UUID> {
  List<InventoryDocumentLine> findAllByInventoryDocumentId(UUID inventoryDocumentId);

  @org.springframework.data.jpa.repository.Query("""
      SELECT line FROM InventoryDocumentLine line
      JOIN FETCH line.inventoryDocument doc
      WHERE line.product.id = :productId
        AND doc.warehouse.id = :warehouseId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.RECEIPT, com.dut.erp.enums.DocumentType.TRANSFER_IN)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity > 0))
        AND line.remainingQuantity > 0
      ORDER BY doc.dateDone ASC, line.createdAt ASC
      """)
  List<InventoryDocumentLine> findAvailableInboundLayersFifo(
      @org.springframework.data.repository.query.Param("productId") UUID productId, 
      @org.springframework.data.repository.query.Param("warehouseId") UUID warehouseId);

  @org.springframework.data.jpa.repository.Query("""
      SELECT line FROM InventoryDocumentLine line
      JOIN FETCH line.inventoryDocument doc
      WHERE line.product.id = :productId
        AND doc.warehouse.id = :warehouseId
        AND doc.documentStatus = com.dut.erp.enums.DocumentStatus.COMPLETED
        AND (doc.documentType IN (com.dut.erp.enums.DocumentType.RECEIPT, com.dut.erp.enums.DocumentType.TRANSFER_IN)
             OR (doc.documentType = com.dut.erp.enums.DocumentType.ADJUSTMENT AND line.quantity > 0))
        AND line.remainingQuantity > 0
      ORDER BY doc.dateDone DESC, line.createdAt DESC
      """)
  List<InventoryDocumentLine> findAvailableInboundLayersLifo(
      @org.springframework.data.repository.query.Param("productId") UUID productId, 
      @org.springframework.data.repository.query.Param("warehouseId") UUID warehouseId);
}
