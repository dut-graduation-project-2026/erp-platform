package com.dut.erp.dto.response;

import com.dut.erp.enums.CostMethod;
import java.util.UUID;

public record ProductTemplateResponse(
    UUID id,
    String name,
    String description,
    String category,
    String uom,
    CostMethod valuationMethod,
    Boolean isActive,
    OrganizationBaseResponse organization) {}
