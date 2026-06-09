package com.dut.erp.repository;

import com.dut.erp.entity.Lead;
import com.dut.erp.repository.projection.CrmBySalespersonProjection;
import com.dut.erp.repository.projection.CrmStageDistributionProjection;
import com.dut.erp.repository.projection.CrmSummaryProjection;
import com.dut.erp.repository.projection.LeadTrendProjection;
import java.time.Instant;
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

  // --- CRM Analytics Queries ---

  @Query(
      value =
          """
          SELECT
              COUNT(l.id) AS totalLeads,
              COUNT(CASE WHEN l.stage = 'WON' THEN 1 END) AS wonLeads,
              COUNT(CASE WHEN l.stage = 'LOST' THEN 1 END) AS lostLeads,
              COALESCE(SUM(l.expected_revenue), 0) AS expectedRevenue,
              COALESCE(SUM(CASE WHEN l.stage = 'WON' THEN l.expected_revenue ELSE 0 END), 0) AS wonRevenue
          FROM leads l
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          """,
      nativeQuery = true)
  CrmSummaryProjection getCrmSummary(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              l.stage AS stage,
              COUNT(l.id) AS leadCount,
              COALESCE(SUM(l.expected_revenue), 0) AS expectedRevenue
          FROM leads l
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          GROUP BY l.stage
          """,
      nativeQuery = true)
  List<CrmStageDistributionProjection> getCrmStageDistribution(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(l.created_at, 'YYYY-MM-DD') AS dateVal,
              COUNT(l.id) AS leadCount
          FROM leads l
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          GROUP BY TO_CHAR(l.created_at, 'YYYY-MM-DD')
          ORDER BY dateVal ASC
          """,
      nativeQuery = true)
  List<LeadTrendProjection> getDailyLeadTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(DATE_TRUNC('week', l.created_at), 'YYYY-MM-DD') AS dateVal,
              COUNT(l.id) AS leadCount
          FROM leads l
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          GROUP BY DATE_TRUNC('week', l.created_at)
          ORDER BY DATE_TRUNC('week', l.created_at) ASC
          """,
      nativeQuery = true)
  List<LeadTrendProjection> getWeeklyLeadTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              TO_CHAR(DATE_TRUNC('month', l.created_at), 'YYYY-MM') AS dateVal,
              COUNT(l.id) AS leadCount
          FROM leads l
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          GROUP BY DATE_TRUNC('month', l.created_at)
          ORDER BY DATE_TRUNC('month', l.created_at) ASC
          """,
      nativeQuery = true)
  List<LeadTrendProjection> getMonthlyLeadTrend(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          """
          SELECT
              u.id AS salespersonId,
              COALESCE(CONCAT(u.first_name, ' ', u.last_name), 'Unassigned') AS salespersonName,
              COUNT(l.id) AS totalLeads,
              COUNT(CASE WHEN l.stage = 'WON' THEN 1 END) AS wonLeads,
              COALESCE(SUM(l.expected_revenue), 0) AS expectedRevenue,
              COALESCE(SUM(CASE WHEN l.stage = 'WON' THEN l.expected_revenue ELSE 0 END), 0) AS wonRevenue
          FROM leads l
          LEFT JOIN users u ON l.sale_person_id = u.id
          WHERE l.organization_id = :organizationId
            AND l.created_at >= :startDate
            AND l.created_at <= :endDate
          GROUP BY u.id, u.first_name, u.last_name
          ORDER BY totalLeads DESC
          """,
      nativeQuery = true)
  List<CrmBySalespersonProjection> getCrmBySalesperson(
      @Param("organizationId") UUID organizationId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);
}
