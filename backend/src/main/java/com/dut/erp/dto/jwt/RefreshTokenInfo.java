package com.dut.erp.dto.jwt;

import com.dut.erp.entity.RefreshToken;
import com.dut.erp.entity.User;
import java.time.Instant;

public record RefreshTokenInfo(String token, String jti, Instant expiresAt) {
  public RefreshToken toEntity(User user) {
    return RefreshToken.builder().user(user).jti(jti).expiresAt(expiresAt).build();
  }
}
