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

  @Query("""
      SELECT w.id
      FROM Warehouse w
      WHERE w.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT w.id
      FROM Warehouse w
      WHERE w.organization.id = :organizationId
        AND (w.manager.id = :userId OR :userId IN (SELECT s.id FROM w.staff s))
      """)
  Page<UUID> findIdsByOrganizationIdAndUserId(
      @Param("organizationId") UUID organizationId,
      @Param("userId") UUID userId,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT w FROM Warehouse w
      LEFT JOIN FETCH w.manager
      WHERE w.id IN :ids
      """)
  List<Warehouse> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT w FROM Warehouse w
      LEFT JOIN FETCH w.organization
      LEFT JOIN FETCH w.manager
      LEFT JOIN FETCH w.staff
      LEFT JOIN FETCH w.createdBy
      LEFT JOIN FETCH w.updatedBy
      WHERE w.id = :id AND w.organization.id = :organizationId
      """)
  Optional<Warehouse> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);

  @Query("""
      SELECT w FROM Warehouse w
      WHERE w.organization.id = :organizationId
      """)
  List<Warehouse> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

  boolean existsByOrganizationIdAndCode(UUID organizationId, String code);

  boolean existsByOrganizationIdAndCodeAndIdNot(UUID organizationId, String code, UUID id);

  @Query("""
      SELECT COUNT(w) > 0 FROM Warehouse w
      WHERE w.id = :id AND w.organization.id = :organizationId
      """)
  boolean existsByOrganizationIdAndIdInternal(
      @Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
