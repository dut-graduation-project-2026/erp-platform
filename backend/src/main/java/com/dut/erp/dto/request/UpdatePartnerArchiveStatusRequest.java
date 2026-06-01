package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePartnerArchiveStatusRequest(@NotNull Boolean isArchived) {}
