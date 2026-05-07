package com.dut.erp.repository;

import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.enums.OrganizationInvitationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationInvitationRepository
    extends JpaRepository<OrganizationInvitation, UUID> {

  @Query(
      """
      SELECT oi
      FROM OrganizationInvitation oi
      JOIN FETCH oi.organization
      JOIN FETCH oi.invitedBy
      WHERE oi.id = :invitationId
      """)
  Optional<OrganizationInvitation> findByIdWithMailContext(@Param("invitationId") UUID invitationId);

  boolean existsByEmailAndOrganizationIdAndStatus(
      String email, UUID organizationId, OrganizationInvitationStatus status);
}
