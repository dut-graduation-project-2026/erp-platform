package com.dut.erp.dto.response;

import java.util.UUID;

public record UserBaseResponse(UUID id, String email, String firstName, String lastName) {}
