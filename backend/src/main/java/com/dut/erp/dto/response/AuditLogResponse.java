package com.dut.erp.dto.response;

import com.dut.erp.enums.ActionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditLogResponse(
    UUID id,
    UUID organizationId,
    String entityType,
    UUID entityId,
    ActionType action,
    String changedField,
    String oldValue,
    String newValue,
    String message,
    UserBaseResponse createdBy,
    Instant createdAt) {}
