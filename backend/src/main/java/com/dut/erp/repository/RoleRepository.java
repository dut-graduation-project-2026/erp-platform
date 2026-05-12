package com.dut.erp.repository;

import com.dut.erp.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByNameAndOrganizationId(String name, UUID organizationId);

  @Query(
      """
      SELECT r.id
      FROM Role r
      WHERE r.organization.id = :organizationId
      """)
  Page<UUID> findRoleIdsByOrganizationId(
      @Param("organizationId") UUID organizationId, Pageable pageable);

  @Query(
      """
      SELECT DISTINCT r FROM Role r
      LEFT JOIN FETCH r.organization o
      WHERE r.id IN :roleIds
      """)
  List<Role> findAllByIdIn(@Param("roleIds") List<UUID> roleIds);

  @Query(
      """
      SELECT r
      FROM Role r
      LEFT JOIN FETCH r.organization
      WHERE r.id = :roleId
      """)
  Optional<Role> findByIdWithOrganization(@Param("roleId") UUID roleId);

  @Query(
      """
      SELECT r
      FROM Role r
      LEFT JOIN FETCH r.organization
      LEFT JOIN FETCH r.permissions p
      LEFT JOIN FETCH p.module
      WHERE r.id = :roleId
      """)
  Optional<Role> findByIdWithOrganizationAndPermissionAndModule(@Param("roleId") UUID roleId);
}
