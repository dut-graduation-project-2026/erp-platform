package com.dut.erp.repository;

import com.dut.erp.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByNameAndOrganizationId(String name, UUID organizationId);

  @Query(
      """
      SELECT DISTINCT r
      FROM User u
      JOIN u.roles r
      WHERE u.id = :userId AND r.organization.id = :organizationId
      """)
  List<Role> findRolesByUserIdAndOrganizationId(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Query("SELECT r FROM Role r WHERE r.organization.id = :organizationId")
  List<Role> findAllByOrganizationId(@Param("organizationId") UUID organizationId);

  @Query(
      """
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM Role r
      JOIN r.permissions p
      JOIN p.actions a
      WHERE r.id = :roleId
        AND p.resource = :permissionName
        AND a.name = :actionName
      """)
  boolean existsByIdAndPermissionNameAndActionName(
      @Param("roleId") UUID roleId,
      @Param("permissionName") String permissionName,
      @Param("actionName") String actionName);
}
