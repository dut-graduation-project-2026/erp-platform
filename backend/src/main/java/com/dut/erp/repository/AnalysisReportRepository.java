package com.dut.erp.repository;

import com.dut.erp.entity.AnalysisReport;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {

  @Query(
      """
      SELECT a FROM AnalysisReport a
      WHERE a.organization.id = :organizationId
        AND a.analysisType = :analysisType
        AND a.createdAt >= :startDate
      ORDER BY a.createdAt DESC
      """)
  List<AnalysisReport> findRecentAnalysis(
      @Param("organizationId") UUID organizationId,
      @Param("analysisType") String analysisType,
      @Param("startDate") Instant startDate);
}
