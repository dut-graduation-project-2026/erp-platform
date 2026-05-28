package com.dut.erp.dto.response;

import com.dut.erp.enums.ActionType;
import java.time.Instant;
import java.util.UUID;

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
    String ipAddress,
    UserBaseResponse createdBy,
    Instant createdAt) {}
