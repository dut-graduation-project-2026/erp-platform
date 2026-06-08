package com.dut.erp.dto.request;

import com.dut.erp.enums.TaxComputation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpsertTaxRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,
    @NotNull(message = "Computation cannot be null")
        TaxComputation computation,
    @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.00", message = "Amount must be greater than or equal to 0.00")
        @Digits(
            integer = 13,
            fraction = 2,
            message = "Amount must be a valid decimal number with up to 13 integer digits and 2 decimal places")
        BigDecimal amount,
    String description
) {}
