package com.dut.erp.repository;

import com.dut.erp.entity.CrmLead;
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
public interface CrmLeadRepository extends JpaRepository<CrmLead, UUID> {

  @Query("""
      SELECT cl.id
      FROM CrmLead cl
      WHERE cl.organization.id = :organizationId
      """)
  Page<UUID> findIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query("""
      SELECT DISTINCT cl FROM CrmLead cl
      LEFT JOIN FETCH cl.organization
      LEFT JOIN FETCH cl.stage
      LEFT JOIN FETCH cl.partner
      WHERE cl.id IN :ids
      """)
  List<CrmLead> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT cl FROM CrmLead cl
      LEFT JOIN FETCH cl.organization
      LEFT JOIN FETCH cl.stage
      LEFT JOIN FETCH cl.partner
      LEFT JOIN FETCH cl.salesperson
      LEFT JOIN FETCH cl.salesTeam
      WHERE cl.id = :id
      """)
  Optional<CrmLead> findByIdWithDetails(@Param("id") UUID id);

  @Query("""
      SELECT COUNT(cl)
      FROM CrmLead cl
      WHERE cl.organization.id = :organizationId
      AND cl.type = :type
      """)
  Long countByOrganizationIdAndType(
      @Param("organizationId") UUID organizationId, @Param("type") String type);

  @Query("""
      SELECT cs.name, COUNT(cl), COALESCE(SUM(cl.expectedRevenue), 0)
      FROM CrmLead cl
      JOIN cl.stage cs
      WHERE cl.organization.id = :organizationId
      GROUP BY cs.name
      ORDER BY cs.sequence ASC
      """)
  List<Object[]> countAndSumRevenueByStage(@Param("organizationId") UUID organizationId);
}
