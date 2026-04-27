package com.dut.erp.service;

import java.util.UUID;

public interface SecurityService {
  boolean isMemberOfOrganization(UUID userId, UUID organizationId);
  boolean hasAuthority(UUID userId, UUID organizationId, String authority);
}
