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
      """)
  Page<UUID> findIdsByWarehouseId(@Param("warehouseId") UUID warehouseId, Pageable pageable);

  @Query("""
      SELECT d.id FROM InventoryDocument d
      WHERE d.warehouse.id = :warehouseId
        AND LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByWarehouseIdAndSearch(
      @Param("warehouseId") UUID warehouseId, @Param("search") String search, Pageable pageable);

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

  Optional<InventoryDocument> findByReferenceTypeAndReferenceIdAndDocumentType(
      ReferenceType referenceType, UUID referenceId, DocumentType documentType);

  boolean existsByName(String name);
}
