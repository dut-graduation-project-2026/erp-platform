package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartnerContactResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String jobPosition,
    String notes) {}
