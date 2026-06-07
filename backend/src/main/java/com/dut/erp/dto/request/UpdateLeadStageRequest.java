package com.dut.erp.dto.request;

import com.dut.erp.annotation.ValueOfEnum;
import com.dut.erp.enums.LeadStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateLeadStageRequest(
    @Pattern(regexp = "^\\S+$", message = "Lead stage must not contain whitespace")
        @NotNull(message = "Lead stage cannot be null")
        @ValueOfEnum(
            enumClass = LeadStage.class,
            message = "Lead stage must be one of: {enumValues}")
        String stage) {}
