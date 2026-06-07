package com.dut.erp.dto.response;

import com.dut.erp.enums.LeadStage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LeadBaseResponse(
    UUID id,
    String name,
    String taxCode,
    String email,
    String phone,
    BigDecimal expectedRevenue,
    LeadStage stage,
    BigDecimal probability,
    UserBaseResponse salePerson,
    PartnerBaseResponse partner,
    Instant createdAt,
    Instant updatedAt) {}
