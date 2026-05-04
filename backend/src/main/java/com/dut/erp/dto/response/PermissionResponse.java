package com.dut.erp.dto.response;

import java.util.UUID;

public record PermissionResponse(UUID id, String name, String description, String code) {}
