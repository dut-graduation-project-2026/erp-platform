package com.dut.erp.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizationResponse(
    UUID id, String name, String description, String hotline, String address, String taxCode, String role) {}
