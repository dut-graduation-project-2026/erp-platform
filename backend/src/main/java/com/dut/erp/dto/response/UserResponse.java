package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    Set<OrganizationBaseResponse> organizations) {}
