package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateArchiveStatusRequest(@NotNull Boolean isArchived) {}
