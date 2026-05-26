package com.dut.erp.repository;

import com.dut.erp.entity.StockInventory;
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
public interface StockInventoryRepository extends JpaRepository<StockInventory, UUID> {

  Optional<StockInventory> findByIdAndOrganizationId(UUID id, UUID organizationId);

  @Query("""
      SELECT si.id
      FROM StockInventory si
      WHERE si.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT si FROM StockInventory si
      LEFT JOIN FETCH si.organization
      LEFT JOIN FETCH si.location
      WHERE si.id IN :ids
      """)
  List<StockInventory> findAllByIdIn(@Param("ids") List<UUID> ids);
}
