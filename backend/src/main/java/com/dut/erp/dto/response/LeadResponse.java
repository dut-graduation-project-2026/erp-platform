package com.dut.erp.dto.response;

import com.dut.erp.enums.LeadStage;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LeadResponse(
    UUID id,
    String name,
    String taxCode,
    String email,
    String phone,
    String address,
    String notes,
    BigDecimal expectedRevenue,
    LeadStage stage,
    BigDecimal probability,
    UserBaseResponse salePerson,
    SaleTeamBaseResponse saleTeam,
    PartnerResponse partner,
    UUID organizationId,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
