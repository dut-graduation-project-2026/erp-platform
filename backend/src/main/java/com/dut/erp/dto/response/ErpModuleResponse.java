package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErpModuleResponse(
    UUID id, String name, String code, String description, Set<PermissionResponse> permissions) {}
