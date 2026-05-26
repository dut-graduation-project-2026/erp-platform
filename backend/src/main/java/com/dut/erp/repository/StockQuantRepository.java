package com.dut.erp.repository;

import com.dut.erp.entity.StockQuant;
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
public interface StockQuantRepository extends JpaRepository<StockQuant, UUID> {

  @Query("""
      SELECT sq FROM StockQuant sq
      WHERE sq.product.id = :productId
        AND sq.location.id = :locationId
        AND ((:lotId IS NULL AND sq.lot IS NULL) OR (sq.lot.id = :lotId))
      """)
  Optional<StockQuant> findByProductIdLocationIdAndLotId(
      @Param("productId") UUID productId,
      @Param("locationId") UUID locationId,
      @Param("lotId") UUID lotId
  );

  @Query("""
      SELECT sq.id
      FROM StockQuant sq
      WHERE sq.location.warehouse.organization.id = :organizationId
        AND (:productId IS NULL OR sq.product.id = :productId)
        AND (:warehouseId IS NULL OR sq.location.warehouse.id = :warehouseId)
        AND (:locationId IS NULL OR sq.location.id = :locationId)
      """)
  Page<UUID> findIdsByFilters(
      @Param("organizationId") UUID organizationId,
      @Param("productId") UUID productId,
      @Param("warehouseId") UUID warehouseId,
      @Param("locationId") UUID locationId,
      Pageable pageable
  );

  @Query("""
      SELECT DISTINCT sq FROM StockQuant sq
      LEFT JOIN FETCH sq.product
      LEFT JOIN FETCH sq.location
      LEFT JOIN FETCH sq.location.warehouse
      LEFT JOIN FETCH sq.lot
      WHERE sq.id IN :ids
      """)
  List<StockQuant> findAllByIdIn(@Param("ids") List<UUID> ids);

  List<StockQuant> findAllByProductId(UUID productId);

  @Query("""
      SELECT sq FROM StockQuant sq
      WHERE sq.product.id = :productId
        AND sq.location.locationType = 'INTERNAL'
        AND sq.quantity > sq.reservedQuantity
      """)
  List<StockQuant> findAvailableQuantsByProduct(@Param("productId") UUID productId);

  @Query("""
      SELECT sq.product.id, SUM(sq.quantity)
      FROM StockQuant sq
      WHERE sq.location.warehouse.organization.id = :organizationId
        AND sq.location.locationType = 'INTERNAL'
      GROUP BY sq.product.id
      """)
  List<Object[]> sumQuantityByProductForOrganization(@Param("organizationId") UUID organizationId);

  @Query("""
      SELECT sq FROM StockQuant sq
      WHERE sq.location.warehouse.organization.id = :organizationId
        AND sq.lot IS NOT NULL
        AND sq.lot.expirationDate <= :expiryThreshold
        AND sq.quantity > 0
      """)
  List<StockQuant> findExpiringQuantsByOrganization(
      @Param("organizationId") UUID organizationId,
      @Param("expiryThreshold") java.time.Instant expiryThreshold
  );
}
