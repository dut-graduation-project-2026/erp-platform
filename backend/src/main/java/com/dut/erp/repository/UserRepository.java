package com.dut.erp.repository;

import com.dut.erp.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
  @Query(
      """
          SELECT DISTINCT u
          FROM User u
          LEFT JOIN FETCH u.roles r
          WHERE u.id = :id
      """)
  Optional<User> findByIdWithRoles(@Param("id") UUID userId);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  @Query(
      """
          SELECT DISTINCT u
          FROM User u
          LEFT JOIN u.organizations o
          WHERE u.id = :userId AND o.id = :organizationId
      """)
  Optional<User> findByIdAndOrganizationId(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Query(
      """
          SELECT COUNT(u) > 0
          FROM User u
          JOIN u.organizations o
          JOIN u.roles r
          JOIN r.permissions p
          WHERE u.id = :userId AND o.id = :organizationId AND p.resource = :permissionResource AND p.action = :permissionAction
      """)
  boolean existsByIdAndAuthoritiesPermissionResourceAndAuthoritiesPermissionAction(
      UUID userId, String permissionResource, String permissionAction);
}
