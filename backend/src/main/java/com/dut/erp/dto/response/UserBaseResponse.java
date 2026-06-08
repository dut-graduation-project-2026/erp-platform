package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserBaseResponse(UUID id, String email, String firstName, String lastName) {}
