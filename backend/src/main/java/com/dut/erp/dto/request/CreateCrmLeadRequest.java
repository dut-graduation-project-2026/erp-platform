package com.dut.erp.dto.request;

import com.dut.erp.enums.LeadType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateCrmLeadRequest(
    @NotBlank(message = "Lead name cannot be blank")
        @Size(max = 255, message = "Lead name must not exceed 255 characters")
        String name,
    LeadType type,
    UUID partnerId,
    BigDecimal expectedRevenue,
    @DecimalMin(value = "0.00") @DecimalMax(value = "100.00") BigDecimal probability,
    @NotNull(message = "Stage ID is required") UUID stageId,
    UUID salespersonId,
    UUID salesTeamId) {}
