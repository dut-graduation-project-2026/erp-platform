package com.dut.erp.service;

import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;

public interface SecurityAuthService {
  boolean hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails);
  boolean hasPermission(String permissionCode, UUID organizationId, CustomUserDetails userDetails);
  boolean hasModuleAccess(String moduleCode, UUID organizationId, CustomUserDetails userDetails);

  boolean isAdmin(CustomUserDetails userDetails);
  boolean isWarehouseManagerOrAdmin(Warehouse warehouse, CustomUserDetails userDetails);
  boolean isWarehouseStaffOrManagerOrAdmin(Warehouse warehouse, CustomUserDetails userDetails);
  boolean isLeadOwnerOrManagerOrAdmin(Lead lead, CustomUserDetails userDetails);
  boolean isOrderOwnerOrManagerOrAdmin(Order order, CustomUserDetails userDetails);
}
