package com.dut.erp.dto.response;

import com.dut.erp.dto.jwt.TokenPair;
import lombok.Builder;

@Builder
public record AuthResponse(UserResponse user, TokenPair tokens) {}
