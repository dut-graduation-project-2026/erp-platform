package com.dut.erp.repository;

import com.dut.erp.entity.ReplenishmentRequest;
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
public interface ReplenishmentRequestRepository extends JpaRepository<ReplenishmentRequest, UUID> {

  @Query("""
      SELECT r.id FROM ReplenishmentRequest r
      WHERE r.warehouse.id = :warehouseId
        AND (:status IS NULL OR r.status = :status)
      """)
  Page<UUID> findIdsByWarehouseId(
      @Param("warehouseId") UUID warehouseId,
      @Param("status") com.dut.erp.enums.ReplenishmentStatus status,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT r FROM ReplenishmentRequest r
      LEFT JOIN FETCH r.warehouse
      LEFT JOIN FETCH r.inventoryDocument
      LEFT JOIN FETCH r.createdBy
      WHERE r.id IN :ids
      """)
  List<ReplenishmentRequest> findAllByIdIn(@Param("ids") List<UUID> ids);

  List<ReplenishmentRequest> findAllByWarehouseId(UUID warehouseId);

  Optional<ReplenishmentRequest> findFirstByInventoryDocumentId(UUID inventoryDocumentId);

  default Optional<ReplenishmentRequest> findByInventoryDocumentId(UUID inventoryDocumentId) {
    return findFirstByInventoryDocumentId(inventoryDocumentId);
  }

  @Query("""
      SELECT r.id FROM ReplenishmentRequest r
      LEFT JOIN r.inventoryDocument d
      LEFT JOIN Order o ON d.referenceType = com.dut.erp.enums.ReferenceType.SALES_ORDER AND d.referenceId = o.id
      WHERE r.warehouse.id = :warehouseId
        AND (:status IS NULL OR r.status = :status)
        AND (LOWER(r.notes) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR (d.referenceType = com.dut.erp.enums.ReferenceType.SALES_ORDER AND LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%'))))
      """)
  Page<UUID> findIdsByWarehouseIdAndSearch(
      @Param("warehouseId") UUID warehouseId,
      @Param("search") String search,
      @Param("status") com.dut.erp.enums.ReplenishmentStatus status,
      Pageable pageable);
}
