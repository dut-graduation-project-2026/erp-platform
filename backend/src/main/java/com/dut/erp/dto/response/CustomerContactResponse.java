package com.dut.erp.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CustomerContactResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String position,
    Boolean isPrimary,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy
) {}
