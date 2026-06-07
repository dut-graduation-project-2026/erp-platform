package com.dut.erp.dto.response;

import java.util.UUID;

public record SaleTeamBaseResponse(
    UUID id,
    String name,
    Boolean isArchived) {}
