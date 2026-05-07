package com.dut.erp.repository;

import com.dut.erp.entity.OrganizationInvitation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationInvitationRepository
    extends JpaRepository<OrganizationInvitation, UUID> {}
