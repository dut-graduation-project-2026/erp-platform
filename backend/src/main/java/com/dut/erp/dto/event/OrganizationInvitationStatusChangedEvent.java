package com.dut.erp.dto.event;

import com.dut.erp.enums.OrganizationInvitationStatus;
import java.util.UUID;

public record OrganizationInvitationStatusChangedEvent(
    UUID invitationId,
    OrganizationInvitationStatus status
) {}
