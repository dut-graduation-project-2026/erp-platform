package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateArchiveStatusRequest(
    @NotNull(message = "isArchived cannot be null")
    Boolean isArchived
) {}
