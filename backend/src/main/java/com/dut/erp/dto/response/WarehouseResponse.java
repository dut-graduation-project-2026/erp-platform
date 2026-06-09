package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WarehouseResponse(
    UUID id,
    String code,
    String name,
    String address,
    String description,
    boolean isActive,
    BigDecimal maximumCapacity,
    BigDecimal usedCapacity,
    OrganizationBaseResponse organization,
    UserBaseResponse manager,
    List<UserBaseResponse> staff
) {}
