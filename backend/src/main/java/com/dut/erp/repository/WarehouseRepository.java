package com.dut.erp.repository;

import com.dut.erp.entity.Warehouse;
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
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

  Optional<Warehouse> findByIdAndOrganizationId(UUID id, UUID organizationId);

  Optional<Warehouse> findByCodeAndOrganizationId(String code, UUID organizationId);

  @Query("""
      SELECT w.id
      FROM Warehouse w
      WHERE w.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT w FROM Warehouse w
      LEFT JOIN FETCH w.organization
      WHERE w.id IN :ids
      """)
  List<Warehouse> findAllByIdIn(@Param("ids") List<UUID> ids);
}
