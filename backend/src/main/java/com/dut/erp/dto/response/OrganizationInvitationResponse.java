package com.dut.erp.dto.response;

import com.dut.erp.enums.OrganizationInvitationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizationInvitationResponse(
    UUID id,
    String email,
    RoleBaseResponse role,
    OrganizationBaseResponse organization,
    OrganizationInvitationStatus status,
    UserBaseResponse invitedBy,
    UserBaseResponse respondedBy,
    Instant expiresAt,
    Instant createdAt) {}
