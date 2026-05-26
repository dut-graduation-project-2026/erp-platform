package com.dut.erp.dto.request;

import com.dut.erp.enums.CostMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductTemplateRequest(
    @NotBlank(message = "Template name cannot be blank")
        @Size(max = 255, message = "Template name must not exceed 255 characters")
        String name,
    String description,
    @Size(max = 100, message = "Category name must not exceed 100 characters")
        String category,
    @Size(max = 50, message = "UoM must not exceed 50 characters")
        String uom,
    @NotNull(message = "Valuation method is required")
        CostMethod valuationMethod) {}
