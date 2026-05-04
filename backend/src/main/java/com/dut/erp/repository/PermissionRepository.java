package com.dut.erp.repository;

import com.dut.erp.entity.Permission;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  @Query(
      """
      SELECT CASE WHEN COUNT(DISTINCT p.id) > 0 THEN true ELSE false END
      FROM User u
      JOIN u.organizations o
      JOIN u.roles r
      JOIN r.permissions p
      WHERE u.id = :userId
        AND o.id = :organizationId
        AND r.organization.id = :organizationId
        AND p.code = :permissionCode
      """)
  boolean existsByCodeAndUserIdAndOrganizationId(
      @Param("permissionCode") String permissionCode,
      @Param("userId") UUID userId,
      @Param("organizationId") UUID organizationId);
}
