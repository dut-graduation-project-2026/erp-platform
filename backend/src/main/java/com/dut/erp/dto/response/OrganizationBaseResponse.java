package com.dut.erp.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record OrganizationBaseResponse(UUID id, String name) {}
