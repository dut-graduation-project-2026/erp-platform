package com.dut.erp.dto.response;

import java.util.UUID;

public record WarehouseResponse(
    UUID id,
    String code,
    String name,
    String address,
    Boolean isActive,
    OrganizationBaseResponse organization) {}
