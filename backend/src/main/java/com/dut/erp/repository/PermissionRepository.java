package com.dut.erp.repository;

import com.dut.erp.entity.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  Optional<Permission> findByNameAndOrganizationId(String name, UUID organizationId);

  @Query(
      """
      SELECT DISTINCT p
      FROM User u
      JOIN u.roles r
      JOIN r.permissions p
      JOIN FETCH p.organization
      LEFT JOIN FETCH p.actions
      WHERE u.id = :userId AND p.organization.id = :organizationId
      """)
  List<Permission> findAllByOrganizationIdAndUserIdWithActions(@Param("organizationId") UUID organizationId, @Param("userId") UUID userId);
}
