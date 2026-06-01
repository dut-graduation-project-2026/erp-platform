package com.dut.erp.dto.response;

import com.dut.erp.enums.PartnerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartnerResponse(
    UUID id,
    String name,
    String taxCode,
    String email,
    String phone,
    String address,
    String jobPosition,
    String notes,
    Boolean isArchived,
    PartnerType partnerType,
    UUID organizationId,
    List<PartnerContactResponse> contacts,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy,
    Instant createdAt,
    Instant updatedAt) {}
