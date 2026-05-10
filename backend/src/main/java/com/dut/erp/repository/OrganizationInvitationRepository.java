package com.dut.erp.repository;

import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.enums.OrganizationInvitationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationInvitationRepository
    extends JpaRepository<OrganizationInvitation, UUID> {

  @Query(
      """
      SELECT oi
      FROM OrganizationInvitation oi
      JOIN FETCH oi.organization
      JOIN FETCH oi.invitedBy
      LEFT JOIN FETCH oi.role
      LEFT JOIN FETCH oi.acceptedBy
      WHERE oi.id = :id
      """)
  Optional<OrganizationInvitation> findByIdWithContext(@Param("id") UUID id);

  @Query(
      """
      UPDATE OrganizationInvitation oi
      SET oi.status = 'EXPIRED'
      WHERE oi.expirationTime <= :now
          AND oi.status = 'PENDING'
      """)
  int updateExpiredInvitations(@Param("now") Instant now);

  @Query(
      """
      DELETE FROM OrganizationInvitation oi
      WHERE oi.expirationTime <= :now
          AND oi.status = 'EXPIRED'
      """)
  int deleteExpiredInvitations(@Param("now") Instant now);

  boolean existsByEmailAndOrganizationIdAndStatus(
      String email, UUID organizationId, OrganizationInvitationStatus status);
}
