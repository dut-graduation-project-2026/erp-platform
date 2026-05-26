package com.dut.erp.repository;

import com.dut.erp.entity.StockLocation;
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
public interface StockLocationRepository extends JpaRepository<StockLocation, UUID> {

  Optional<StockLocation> findByIdAndWarehouseOrganizationId(UUID id, UUID organizationId);

  Optional<StockLocation> findByCodeAndWarehouseId(String code, UUID warehouseId);

  @Query("""
      SELECT sl FROM StockLocation sl
      WHERE sl.code = :code
        AND sl.warehouse.organization.id = :organizationId
      """)
  List<StockLocation> findAllByCodeAndOrganizationId(
      @Param("code") String code,
      @Param("organizationId") UUID organizationId
  );

  @Query("""
      SELECT sl.id
      FROM StockLocation sl
      WHERE sl.warehouse.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT sl.id
      FROM StockLocation sl
      WHERE sl.warehouse.id = :warehouseId
      """)
  Page<UUID> findIdsByWarehouseId(@Param("warehouseId") UUID warehouseId, Pageable pageable);

  @Query("""
      SELECT DISTINCT sl FROM StockLocation sl
      LEFT JOIN FETCH sl.warehouse
      LEFT JOIN FETCH sl.parent
      WHERE sl.id IN :ids
      """)
  List<StockLocation> findAllByIdIn(@Param("ids") List<UUID> ids);
}
