package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WarehouseBaseResponse(
    UUID id,
    String code,
    String name,
    String address,
    String description,
    boolean isActive,
    UserBaseResponse manager,
    int staffCount,
    java.util.List<UserBaseResponse> staff
) {}
