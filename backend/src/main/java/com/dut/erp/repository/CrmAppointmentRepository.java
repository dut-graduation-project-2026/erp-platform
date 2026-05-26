package com.dut.erp.repository;

import com.dut.erp.entity.CrmAppointment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CrmAppointmentRepository extends JpaRepository<CrmAppointment, UUID> {

  @Query("""
      SELECT ca.id
      FROM CrmAppointment ca
      WHERE ca.organization.id = :organizationId
      AND (cast(:leadId as uuid) IS NULL OR ca.lead.id = :leadId)
      """)
  Page<UUID> findIdsByOrganizationIdAndLeadId(
      @Param("organizationId") UUID organizationId,
      @Param("leadId") UUID leadId,
      Pageable pageable);

  @Query("""
      SELECT DISTINCT ca FROM CrmAppointment ca
      LEFT JOIN FETCH ca.lead
      LEFT JOIN FETCH ca.organization
      WHERE ca.id IN :ids
      """)
  List<CrmAppointment> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("""
      SELECT ca FROM CrmAppointment ca
      LEFT JOIN FETCH ca.lead
      WHERE ca.lead.id = :leadId
      ORDER BY ca.startTime ASC
      """)
  List<CrmAppointment> findAllByLeadId(@Param("leadId") UUID leadId);
}
