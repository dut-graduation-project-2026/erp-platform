package com.dut.erp.service.impl;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.mapper.OrganizationMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.OrganizationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
  private final OrganizationMapper organizationMapper;
  private final OrganizationRepository organizationRepository;

  @Override
  @Transactional(readOnly = true)
  public List<OrganizationResponse> getOrganizationsByUserId(UUID userId) {
    return organizationRepository.findAllWithUserId(userId).stream()
        .map(organizationMapper::toOrganizationResponse)
        .toList();
  }

  @Transactional
  public Organization getOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
  }
}
