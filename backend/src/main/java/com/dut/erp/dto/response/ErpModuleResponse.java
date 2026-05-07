package com.dut.erp.dto.response;

import java.util.Set;
import java.util.UUID;

public record ErpModuleResponse(
    UUID id, String name, String code, String description, Set<PermissionResponse> permissions) {}
