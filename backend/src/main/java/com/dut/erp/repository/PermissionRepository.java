package com.dut.erp.repository;

import com.dut.erp.entity.Permission;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  @Query(
      """
      SELECT DISTINCT p
      FROM Permission p
      JOIN FETCH p.organization
      LEFT JOIN FETCH p.actions
      WHERE p.organization.id = :organizationId
      """)
  List<Permission> findAllByOrganizationIdWithActions(@Param("organizationId") UUID organizationId);

  List<Permission> findAllByIdsIn(Set<UUID> permissionIds);

  @Query(
      """
      SELECT COUNT(p) > 0
      FROM User u
      JOIN u.organizations o
      JOIN u.roles r
      JOIN r.permissions p
      JOIN p.actions a
      WHERE u.id = :userId
        AND o.id = :organizationId
        AND r.organization.id = :organizationId
        AND p.organization.id = :organizationId
        AND p.name = :permissionResource
        AND a.name = :permissionAction
      """)
  boolean existsByUserIdAndOrganizationIdAndPermissionResourceAndPermissionAction(
      @Param("userId") UUID userId,
      @Param("organizationId") UUID organizationId,
      @Param("permissionResource") String permissionResource,
      @Param("permissionAction") String permissionAction);
}
