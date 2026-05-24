package com.dut.erp.dto.response;

import java.util.List;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPermissionsResponse(List<PermissionResponse> permissions) {}
