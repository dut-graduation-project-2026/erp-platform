package com.dut.erp.dto.response;

import com.dut.erp.dto.jwt.TokenPair;

public record AuthResponse(UserResponse user, TokenPair tokens) {}
