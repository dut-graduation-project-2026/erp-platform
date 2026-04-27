package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PermissionResponse(
    UUID id,
    String name,
    String resource,
    String description,
    Set<ActionBaseResponse> actions,
    OrganizationBaseResponse organization) {}
