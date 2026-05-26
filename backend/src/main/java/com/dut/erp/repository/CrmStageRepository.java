package com.dut.erp.repository;

import com.dut.erp.entity.CrmStage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CrmStageRepository extends JpaRepository<CrmStage, UUID> {

  @Query("""
      SELECT cs FROM CrmStage cs
      WHERE cs.organization.id = :organizationId
      ORDER BY cs.sequence ASC
      """)
  List<CrmStage> findAllByOrganizationIdOrderBySequence(
      @Param("organizationId") UUID organizationId);
}
