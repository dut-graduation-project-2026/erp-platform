package com.dut.erp.repository;

import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.ReferenceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryDocumentRepository extends JpaRepository<InventoryDocument, UUID> {

  @Query("""
      SELECT d.id FROM InventoryDocument d
      WHERE d.warehouse.id = :warehouseId
        AND (:status IS NULL OR d.documentStatus = :status)
        AND (:useTypeFilter = false OR d.documentType IN :types)
      ORDER BY CASE d.documentStatus
        WHEN com.dut.erp.enums.DocumentStatus.COMPLETED THEN 3
        WHEN com.dut.erp.enums.DocumentStatus.CANCELLED THEN 2
        ELSE 1
      END ASC, COALESCE(d.updatedAt, d.createdAt) DESC, d.id ASC
      """)
  Page<UUID> findIdsByWarehouseId(
      @Param("warehouseId") UUID warehouseId,
      @Param("status") com.dut.erp.enums.DocumentStatus status,
      @Param("types") java.util.List<com.dut.erp.enums.DocumentType> types,
      @Param("useTypeFilter") boolean useTypeFilter,
      Pageable pageable);

  @Query("""
      SELECT d.id FROM InventoryDocument d
      LEFT JOIN Order o ON d.referenceType = com.dut.erp.enums.ReferenceType.SALES_ORDER AND d.referenceId = o.id
      WHERE d.warehouse.id = :warehouseId
        AND (:status IS NULL OR d.documentStatus = :status)
        AND (:useTypeFilter = false OR d.documentType IN :types)
        AND (LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR (d.referenceType = com.dut.erp.enums.ReferenceType.SALES_ORDER AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))))
      ORDER BY CASE d.documentStatus
        WHEN com.dut.erp.enums.DocumentStatus.COMPLETED THEN 3
        WHEN com.dut.erp.enums.DocumentStatus.CANCELLED THEN 2
        ELSE 1
      END ASC, COALESCE(d.updatedAt, d.createdAt) DESC, d.id ASC
      """)
  Page<UUID> findIdsByWarehouseIdAndSearch(
      @Param("warehouseId") UUID warehouseId,
      @Param("search") String search,
      @Param("status") com.dut.erp.enums.DocumentStatus status,
      @Param("types") java.util.List<com.dut.erp.enums.DocumentType> types,
      @Param("useTypeFilter") boolean useTypeFilter,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT d FROM InventoryDocument d
      LEFT JOIN FETCH d.warehouse
      LEFT JOIN FETCH d.sourceWarehouse
      LEFT JOIN FETCH d.createdBy
      LEFT JOIN FETCH d.updatedBy
      WHERE d.id IN :ids
      """)
  List<InventoryDocument> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT d FROM InventoryDocument d
      LEFT JOIN FETCH d.warehouse
      LEFT JOIN FETCH d.sourceWarehouse
      LEFT JOIN FETCH d.createdBy
      LEFT JOIN FETCH d.updatedBy
      WHERE d.id = :id AND d.warehouse.id = :warehouseId
      """)
  Optional<InventoryDocument> findByIdAndWarehouseId(
      @Param("id") UUID id, @Param("warehouseId") UUID warehouseId);

  boolean existsByReferenceTypeAndReferenceIdAndDocumentTypeAndDocumentStatusNot(
      ReferenceType referenceType, UUID referenceId, DocumentType documentType, DocumentStatus documentStatus);

  List<InventoryDocument> findAllByWarehouseIdAndDocumentStatus(UUID warehouseId, DocumentStatus documentStatus);

  Optional<InventoryDocument> findFirstByReferenceTypeAndReferenceIdAndDocumentTypeOrderByCreatedAtDesc(
      ReferenceType referenceType, UUID referenceId, DocumentType documentType);

  default Optional<InventoryDocument> findByReferenceTypeAndReferenceIdAndDocumentType(
      ReferenceType referenceType, UUID referenceId, DocumentType documentType) {
    return findFirstByReferenceTypeAndReferenceIdAndDocumentTypeOrderByCreatedAtDesc(referenceType, referenceId, documentType);
  }

  @Query("""
      SELECT d FROM InventoryDocument d
      LEFT JOIN FETCH d.warehouse
      WHERE d.referenceType = :referenceType
        AND d.referenceId = :referenceId
        AND d.documentType = :documentType
        AND d.documentStatus <> com.dut.erp.enums.DocumentStatus.CANCELLED
      ORDER BY d.createdAt DESC
      """)
  List<InventoryDocument> findActiveDocuments(
      @Param("referenceType") ReferenceType referenceType,
      @Param("referenceId") UUID referenceId,
      @Param("documentType") DocumentType documentType);

  @Query("""
      SELECT d FROM InventoryDocument d
      LEFT JOIN FETCH d.warehouse
      WHERE d.referenceType = :referenceType
        AND d.referenceId IN :referenceIds
        AND d.documentType = :documentType
        AND d.documentStatus <> com.dut.erp.enums.DocumentStatus.CANCELLED
      ORDER BY d.createdAt DESC
      """)
  List<InventoryDocument> findActiveDocumentsForOrders(
      @Param("referenceType") ReferenceType referenceType,
      @Param("referenceIds") List<UUID> referenceIds,
      @Param("documentType") DocumentType documentType);

  boolean existsByName(String name);

  @Query("""
      SELECT COUNT(d) FROM InventoryDocument d
      WHERE d.warehouse.id = :warehouseId
        AND d.documentType IN :types
        AND d.documentStatus IN :statuses
      """)
  long countByWarehouseIdAndDocumentTypeInAndDocumentStatusIn(
      @Param("warehouseId") UUID warehouseId,
      @Param("types") List<DocumentType> types,
      @Param("statuses") List<DocumentStatus> statuses
  );

  @Query("""
      SELECT COUNT(d) FROM InventoryDocument d
      WHERE d.warehouse.id = :warehouseId
        AND d.documentType IN :types
        AND d.documentStatus NOT IN :excludedStatuses
        AND d.scheduledDate < CURRENT_TIMESTAMP
      """)
  long countLateDocuments(
      @Param("warehouseId") UUID warehouseId,
      @Param("types") List<DocumentType> types,
      @Param("excludedStatuses") List<DocumentStatus> excludedStatuses
  );
}
