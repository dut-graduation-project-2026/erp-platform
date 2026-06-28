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

  @Query("""
      SELECT p.category.id, p.category.name,
        COALESCE(SUM(ib.quantity * p.purchasePrice), 0),
        COALESCE(SUM(ib.quantity), 0),
        COUNT(DISTINCT p.id)
      FROM InventoryBalance ib
      JOIN ib.product p
      JOIN ib.warehouse w
      WHERE w.organization.id = :orgId
        AND ib.quantity > 0
      GROUP BY p.category.id, p.category.name
      ORDER BY COALESCE(SUM(ib.quantity * p.purchasePrice), 0) DESC
      """)
  List<Object[]> findAssetDistributionByCategory(@Param("orgId") UUID orgId);

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

  @Query(
      """
      SELECT ib FROM InventoryBalance ib
      JOIN FETCH ib.product
      WHERE ib.warehouse.id = :warehouseId AND ib.product.id IN :productIds
      """)
  List<InventoryBalance> findAllByWarehouseIdAndProductIdIn(
      @Param("warehouseId") UUID warehouseId, @Param("productIds") List<UUID> productIds);

  @Query(
      """
      SELECT ib FROM InventoryBalance ib
      JOIN FETCH ib.warehouse w
      WHERE ib.product.id = :productId AND w.organization.id = :organizationId
      """)
  List<InventoryBalance> findAllByProductIdAndOrganizationId(
      @Param("productId") UUID productId, @Param("organizationId") UUID organizationId);

  Optional<InventoryBalance> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

  @Query(
      """
      SELECT ib FROM InventoryBalance ib
      JOIN FETCH ib.warehouse w
      JOIN FETCH ib.product p
      WHERE w.organization.id = :organizationId
      """)
  List<InventoryBalance> findAllByOrganizationId(
      @Param("organizationId") UUID organizationId);
}

