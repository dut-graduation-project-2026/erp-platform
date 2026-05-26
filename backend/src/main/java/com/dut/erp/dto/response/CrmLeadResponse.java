package com.dut.erp.dto.response;

import com.dut.erp.enums.LeadType;
import java.math.BigDecimal;
import java.util.UUID;

public record CrmLeadResponse(
    UUID id,
    String name,
    LeadType type,
    BigDecimal expectedRevenue,
    BigDecimal probability,
    CrmStageResponse stage,
    SalePartnerBaseResponse partner,
    UserBaseResponse salesperson,
    SalesTeamResponse salesTeam) {}
