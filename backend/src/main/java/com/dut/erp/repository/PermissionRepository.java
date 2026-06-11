package com.dut.erp.repository;

import com.dut.erp.entity.Permission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  @Query(
      """
        SELECT CASE WHEN EXISTS (
          SELECT 1
          FROM User u
          JOIN u.roles r
          JOIN r.permissions p
          WHERE u.id = :userId
            AND r.organization.id = :organizationId
            AND p.code = :permissionCode
        ) THEN true ELSE false END
      """)
  boolean existsByUserIdAndOrganizationIdAndPermissionCode(
      @Param("userId") UUID userId,
      @Param("organizationId") UUID organizationId,
      @Param("permissionCode") String permissionCode);

  @Query(
      """
        SELECT DISTINCT p.code
        FROM User u
        JOIN u.roles r
        JOIN r.permissions p
        WHERE u.id = :userId
          AND r.organization.id = :organizationId
      """)
  List<String> findPermissionCodesByUserIdAndOrganizationId(
      @Param("userId") UUID userId,
      @Param("organizationId") UUID organizationId);
}
