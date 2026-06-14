package com.dut.erp.repository;

import com.dut.erp.entity.Lead;
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
public interface LeadRepository extends JpaRepository<Lead, UUID> {

  @Query(
      """
      SELECT l.id
      FROM Lead l
      WHERE l.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query(
      """
      SELECT l.id
      FROM Lead l
      WHERE l.organization.id = :organizationId
      AND (
        LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(l.phone) LIKE LOWER(CONCAT('%', :search, '%'))
      )
      """)
  Page<UUID> findIdsByOrganizationIdAndSearch(
      @Param("organizationId") UUID organizationId,
      @Param("search") String search,
      Pageable pageable);

  @Query(
      """
      SELECT DISTINCT l FROM Lead l
      LEFT JOIN FETCH l.salePerson
      LEFT JOIN FETCH l.saleTeam
      LEFT JOIN FETCH l.partner
      LEFT JOIN FETCH l.organization
      LEFT JOIN FETCH l.createdBy
      LEFT JOIN FETCH l.updatedBy
      WHERE l.id IN :ids
      """)
  List<Lead> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT l.stage, COUNT(l.id)
      FROM Lead l
      WHERE l.organization.id = :orgId
      GROUP BY l.stage
      """)
  List<Object[]> countLeadsByStage(@Param("orgId") UUID orgId);

  @Query(
      """
      SELECT l FROM Lead l
      LEFT JOIN FETCH l.salePerson
      LEFT JOIN FETCH l.saleTeam
      LEFT JOIN FETCH l.partner
      LEFT JOIN FETCH l.organization
      LEFT JOIN FETCH l.createdBy
      LEFT JOIN FETCH l.updatedBy
      WHERE l.id = :id AND l.organization.id = :organizationId
      """)
  Optional<Lead> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("organizationId") UUID organizationId);
}
