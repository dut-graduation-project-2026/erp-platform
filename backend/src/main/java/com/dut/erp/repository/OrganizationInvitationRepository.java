package com.dut.erp.repository;

import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.enums.OrganizationInvitationStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
      LEFT JOIN FETCH oi.respondedBy
      WHERE oi.id = :id
      """)
  Optional<OrganizationInvitation> findByIdWithContext(@Param("id") UUID id);

  @Modifying
  @Query(
      """
      UPDATE OrganizationInvitation oi
      SET oi.status = :expiredStatus
      WHERE oi.expiresAt <= :now
          AND oi.status = :pendingStatus
      """)
  int updateExpiredInvitations(
      @Param("now") Instant now,
      @Param("expiredStatus") OrganizationInvitationStatus expiredStatus,
      @Param("pendingStatus") OrganizationInvitationStatus pendingStatus);

  @Modifying
  @Query(
      """
      DELETE FROM OrganizationInvitation oi
      WHERE oi.expiresAt <= :now
          AND oi.status = :status
      """)
  int deleteExpiredInvitations(
      @Param("now") Instant now, @Param("status") OrganizationInvitationStatus status);

  @Query(
      """
      SELECT oi
      FROM OrganizationInvitation oi
      JOIN FETCH oi.organization
      JOIN FETCH oi.invitedBy
      LEFT JOIN FETCH oi.role
      LEFT JOIN FETCH oi.respondedBy
      WHERE oi.email = :email AND oi.organization.id = :organizationId
      """)
  Optional<OrganizationInvitation> findByEmailAndOrganizationId(
      @Param("email") String email, @Param("organizationId") UUID organizationId);

  boolean existsByEmailAndOrganizationIdAndStatus(
      String email, UUID organizationId, OrganizationInvitationStatus status);

  @Query(
      """
      SELECT oi
      FROM OrganizationInvitation oi
      JOIN FETCH oi.organization
      JOIN FETCH oi.invitedBy
      LEFT JOIN FETCH oi.role
      LEFT JOIN FETCH oi.respondedBy
      WHERE oi.organization.id = :organizationId
      ORDER BY oi.createdAt DESC
      """)
  java.util.List<OrganizationInvitation> findAllByOrganizationId(@Param("organizationId") UUID organizationId);
}
