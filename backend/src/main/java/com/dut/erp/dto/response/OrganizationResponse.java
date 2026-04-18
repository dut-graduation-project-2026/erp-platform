package com.dut.erp.dto.response;

import java.util.UUID;

public record OrganizationResponse(
    UUID id, String name, String description, String hotline, String address) {}
