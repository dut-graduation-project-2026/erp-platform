package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateOrganizationInvitationStatusRequest(
    @NotNull(message = "Accepted field is required") Boolean accepted) {}
