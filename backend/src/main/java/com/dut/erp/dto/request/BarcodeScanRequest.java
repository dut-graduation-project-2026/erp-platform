package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BarcodeScanRequest(
    @NotBlank(message = "Barcode is required")
    String barcode
) {}
