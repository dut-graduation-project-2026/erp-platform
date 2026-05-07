package com.dut.erp.service.impl;

import com.dut.erp.repository.OrganizationInvitationRepository;
import com.dut.erp.service.OrganizationInvitationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationInvitationServiceImpl implements OrganizationInvitationService {
  private final OrganizationInvitationRepository organizationInvitationRepository;

  
}
