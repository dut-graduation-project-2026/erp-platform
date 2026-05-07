package com.dut.erp.repository;

import com.dut.erp.entity.ErpModule;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ErpModuleRepository extends JpaRepository<ErpModule, UUID> {
  @Query(
      """
          SELECT DISTINCT m
          FROM User u
          JOIN u.roles r
          JOIN r.permissions permission
          JOIN permission.module m
          WHERE u.id = :userId
            AND r.organization.id = :organizationId
      """)
  List<ErpModule> findAccessibleModulesByUserAndOrganization(
      @Param("userId") UUID userId, @Param("organizationId") UUID organizationId);

  @Query(
      """
          SELECT DISTINCT m
          FROM Role r
          JOIN r.permissions permission
          JOIN permission.module m
          LEFT JOIN FETCH m.permissions modulePermission
          WHERE r.organization.id = :organizationId
      """)
  List<ErpModule> findByOrganizationIdWithPermissions(@Param("organizationId") UUID organizationId);
}
