package com.dut.erp.repository;

import com.dut.erp.entity.AuditLog;
import com.dut.erp.enums.ActionType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

  /**
   * Find all audit logs for a specific entity (e.g. all logs for a particular partner).
   */
  @Query(
      """
      SELECT a FROM AuditLog a
      WHERE a.entityType = :entityType
        AND a.entityId   = :entityId
      ORDER BY a.createdAt DESC
      """)
  Page<AuditLog> findByEntityTypeAndEntityId(
      @Param("entityType") String entityType,
      @Param("entityId") UUID entityId,
      Pageable pageable);

  /**
   * Find all audit logs for a given entity type within an organization
   * (via the actor's organization membership).
   */
  @Query(
      """
      SELECT a FROM AuditLog a
      WHERE a.entityType     = :entityType
        AND a.organizationId = :organizationId
      ORDER BY a.createdAt DESC
      """)
  Page<AuditLog> findByEntityTypeAndOrganizationId(
      @Param("entityType") String entityType,
      @Param("organizationId") UUID organizationId,
      Pageable pageable);

  /**
   * Find all audit logs created by a specific user within an organization.
   */
  @Query(
      """
      SELECT a FROM AuditLog a
      WHERE a.organizationId  = :organizationId
        AND a.createdBy.id    = :userId
      ORDER BY a.createdAt DESC
      """)
  Page<AuditLog> findByOrganizationIdAndCreatedById(
      @Param("organizationId") UUID organizationId,
      @Param("userId") UUID userId,
      Pageable pageable);

  /**
   * Find all audit logs for an organization filtered by action type.
   */
  @Query(
      """
      SELECT a FROM AuditLog a
      WHERE a.organizationId = :organizationId
        AND a.action         = :action
      ORDER BY a.createdAt DESC
      """)
  Page<AuditLog> findByOrganizationIdAndAction(
      @Param("organizationId") UUID organizationId,
      @Param("action") ActionType action,
      Pageable pageable);
}
