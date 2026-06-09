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
      """)
  Page<UUID> findIdsByWarehouseId(@Param("warehouseId") UUID warehouseId, Pageable pageable);

  @Query("""
      SELECT DISTINCT r FROM ReplenishmentRequest r
      LEFT JOIN FETCH r.warehouse
      LEFT JOIN FETCH r.inventoryDocument
      LEFT JOIN FETCH r.createdBy
      WHERE r.id IN :ids
      """)
  List<ReplenishmentRequest> findAllByIdIn(@Param("ids") List<UUID> ids);

  List<ReplenishmentRequest> findAllByWarehouseId(UUID warehouseId);

  Optional<ReplenishmentRequest> findByInventoryDocumentId(UUID inventoryDocumentId);
}
