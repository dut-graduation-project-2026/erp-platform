package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertOrderItemRequest(
    @NotNull(message = "Product ID cannot be null")
    UUID productId,
    UUID taxId,
    @NotNull(message = "Quantity cannot be null")
    @DecimalMin(value = "0.0001", message = "Quantity must be greater than 0")
    @Digits(integer = 11, fraction = 4, message = "Quantity must have up to 11 integer digits and 4 decimal places")
    BigDecimal quantity,
    @NotNull(message = "Unit price cannot be null")
    @DecimalMin(value = "0.00", message = "Unit price must be greater than or equal to 0.00")
    @Digits(integer = 13, fraction = 2, message = "Unit price must have up to 13 integer digits and 2 decimal places")
    BigDecimal unitPrice
) {}
