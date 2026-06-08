package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PermissionResponse(UUID id, String name, String description, String code) {}
