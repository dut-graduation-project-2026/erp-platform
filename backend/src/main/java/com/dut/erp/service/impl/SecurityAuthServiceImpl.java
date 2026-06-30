package com.dut.erp.service.impl;

import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.repository.ErpModuleRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SecurityAuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("securityAuthService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityAuthServiceImpl implements SecurityAuthService {

  private final PermissionRepository permissionRepository;
  private final ErpModuleRepository erpModuleRepository;

  @Override
  public boolean hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails) {
    boolean hasAccess =
        userDetails.getOrganizations().stream().anyMatch(org -> org.getId().equals(organizationId));
    if (!hasAccess) {
      log.warn("User {} denied access to organization {}", userDetails.getId(), organizationId);
      throw new AccessDeniedException("Access denied");
    }

    return true;
  }

  @Override
  public boolean hasModuleAccess(
      String moduleCode, UUID organizationId, CustomUserDetails userDetails) {
    boolean hasAccess =
        erpModuleRepository.existsByCodeAndOrganizationIdAndUserId(
            moduleCode, organizationId, userDetails.getId());
    if (!hasAccess) {
      log.warn("User {} denied access to module {}", userDetails.getId(), moduleCode);
      throw new AccessDeniedException("Access denied");
    }
    return true;
  }

  @Override
  public boolean hasPermission(
      String permissionCode, UUID organizationId, CustomUserDetails userDetails) {
    boolean hasPermission =
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            userDetails.getId(), organizationId, permissionCode);

    if (!hasPermission) {
      log.warn(
          "User {} missing permission '{}' for organization {}",
          userDetails.getId(),
          permissionCode,
          organizationId);
      throw new AccessDeniedException("Access denied");
    }

    return true;
  }

  @Override
  public boolean isAdmin(CustomUserDetails userDetails) {
    if (userDetails == null) {
      return false;
    }
    return userDetails.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") 
                    || a.getAuthority().equals("ROLE_SYSTEM ADMIN")
                    || a.getAuthority().equals("ROLE_SYSTEM_ADMIN")
                    || a.getAuthority().equals("SUPER_ADMIN"));
  }
 
  @Override
  public boolean isWarehouseManagerOrAdmin(Warehouse warehouse, CustomUserDetails userDetails) {
    if (isAdmin(userDetails)) {
      return true;
    }
    if (warehouse.getManager() != null && warehouse.getManager().getId().equals(userDetails.getId())) {
      return true;
    }
    // Check if user has 'warehouses:write_all' permission in the organization
    boolean hasWriteAllPermission = permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
        userDetails.getId(), warehouse.getOrganization().getId(), "warehouses:write_all"
    );
    if (hasWriteAllPermission) {
      return true;
    }
    log.warn("User {} denied update access to warehouse {}", userDetails.getId(), warehouse.getId());
    throw new AccessDeniedException("Access denied: You must be the warehouse manager or an administrator.");
  }
 
  @Override
  public boolean isWarehouseStaffOrManagerOrAdmin(Warehouse warehouse, CustomUserDetails userDetails) {
    if (isAdmin(userDetails)) {
      return true;
    }
    if (warehouse.getManager() != null && warehouse.getManager().getId().equals(userDetails.getId())) {
      return true;
    }
    boolean isStaff = warehouse.getStaff() != null && warehouse.getStaff().stream()
        .anyMatch(staff -> staff.getId().equals(userDetails.getId()));
    if (isStaff) {
      return true;
    }
    // Check if user has 'warehouses:read_all' permission in the organization
    boolean hasReadAllPermission = permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
        userDetails.getId(), warehouse.getOrganization().getId(), "warehouses:read_all"
    );
    if (hasReadAllPermission) {
      return true;
    }
    log.warn("User {} denied access to warehouse {}", userDetails.getId(), warehouse.getId());
    throw new AccessDeniedException("Access denied: You are not assigned to this warehouse.");
  }

  @Override
  public boolean isLeadOwnerOrManagerOrAdmin(Lead lead, CustomUserDetails userDetails) {
    if (isAdmin(userDetails)) {
      return true;
    }
    // Check if user has 'leads:read_all' permission in the organization
    boolean hasReadAllPermission = permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
        userDetails.getId(), lead.getOrganization().getId(), "leads:read_all"
    );
    if (hasReadAllPermission) {
      return true;
    }
    if (lead.getCreatedBy() != null && lead.getCreatedBy().getId().equals(userDetails.getId())) {
      return true;
    }
    if (lead.getSalePerson() != null && lead.getSalePerson().getId().equals(userDetails.getId())) {
      return true;
    }
    if (lead.getSaleTeam() != null && lead.getSaleTeam().getLeader() != null
        && lead.getSaleTeam().getLeader().getId().equals(userDetails.getId())) {
      return true;
    }
    if (lead.getSaleTeam() != null && lead.getSaleTeam().getMembers() != null
        && lead.getSaleTeam().getMembers().stream().anyMatch(m -> m.getId().equals(userDetails.getId()))) {
      return true;
    }
    log.warn("User {} denied access to lead {}", userDetails.getId(), lead.getId());
    throw new AccessDeniedException("Access denied: You do not own or manage this lead.");
  }

  @Override
  public boolean isOrderOwnerOrManagerOrAdmin(Order order, CustomUserDetails userDetails) {
    if (isAdmin(userDetails)) {
      return true;
    }
    // Check if user has 'orders:read_all' permission in the organization
    boolean hasReadAllPermission = permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
        userDetails.getId(), order.getOrganization().getId(), "orders:read_all"
    );
    if (hasReadAllPermission) {
      return true;
    }
    if (order.getCreatedBy() != null && order.getCreatedBy().getId().equals(userDetails.getId())) {
      return true;
    }
    if (order.getLead() != null) {
      Lead lead = order.getLead();
      if (lead.getCreatedBy() != null && lead.getCreatedBy().getId().equals(userDetails.getId())) {
        return true;
      }
      if (lead.getSalePerson() != null && lead.getSalePerson().getId().equals(userDetails.getId())) {
        return true;
      }
      if (lead.getSaleTeam() != null && lead.getSaleTeam().getLeader() != null
          && lead.getSaleTeam().getLeader().getId().equals(userDetails.getId())) {
        return true;
      }
      if (lead.getSaleTeam() != null && lead.getSaleTeam().getMembers() != null
          && lead.getSaleTeam().getMembers().stream().anyMatch(m -> m.getId().equals(userDetails.getId()))) {
        return true;
      }
    }
    log.warn("User {} denied access to order {}", userDetails.getId(), order.getId());
    throw new AccessDeniedException("Access denied: You do not own or manage this order.");
  }
}
