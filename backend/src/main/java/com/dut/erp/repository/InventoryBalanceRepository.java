package com.dut.erp.repository;

import com.dut.erp.entity.InventoryBalance;
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
public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {

  // ---- Paginated ID list for list-view queries ----

  @Query(
      """
      SELECT ib.id FROM InventoryBalance ib
      JOIN ib.product p
      WHERE ib.warehouse.id = :warehouseId
      """)
  Page<UUID> findIdsByWarehouseId(@Param("warehouseId") UUID warehouseId, Pageable pageable);

  @Query(
      """
      SELECT ib.id FROM InventoryBalance ib
      JOIN ib.product p
      WHERE ib.warehouse.id = :warehouseId
        AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
      """)
  Page<UUID> findIdsByWarehouseIdAndSearch(
      @Param("warehouseId") UUID warehouseId, @Param("search") String search, Pageable pageable);

  // ---- Fetch-join queries for hydrating paginated results ----

  @Query(
      """
      SELECT ib FROM InventoryBalance ib
      JOIN FETCH ib.product
      WHERE ib.id IN :ids
      """)
  List<InventoryBalance> findAllByIdsWithProduct(@Param("ids") List<UUID> ids);

  // ---- Single-record lookups ----

  @Query(
      """
      SELECT ib FROM InventoryBalance ib
      JOIN FETCH ib.warehouse
      JOIN FETCH ib.product
      WHERE ib.id = :id AND ib.warehouse.id = :warehouseId
      """)
  Optional<InventoryBalance> findByIdAndWarehouseId(
      @Param("id") UUID id, @Param("warehouseId") UUID warehouseId);

  Optional<InventoryBalance> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);
}
