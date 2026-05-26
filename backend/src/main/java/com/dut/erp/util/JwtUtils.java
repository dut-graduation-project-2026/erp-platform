package com.dut.erp.util;

import com.dut.erp.config.properties.JwtProperties;
import com.dut.erp.dto.jwt.RefreshTokenInfo;
import com.dut.erp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {
  private static final String TOKEN_TYPE_CLAIM = "TOKEN_TYPE";
  private static final String TOKEN_TYPE_ACCESS = "ACCESS";
  private static final String TOKEN_TYPE_REFRESH = "REFRESH";
  private static final int JTI_LOG_PREFIX_LENGTH = 8;

  private final JwtProperties jwtProperties;
  private final SecretKey signingKey;

  public JwtUtils(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user, Instant now) {
    return generateToken(user, TOKEN_TYPE_ACCESS, now, jwtProperties.accessTokenExpiration());
  }

  public RefreshTokenInfo generateRefreshToken(User user, Instant now) {
    Instant expiryDate = now.plusMillis(jwtProperties.refreshTokenExpiration());
    String jti = UUID.randomUUID().toString();

    String token =
        generateToken(user, TOKEN_TYPE_REFRESH, now, jwtProperties.refreshTokenExpiration(), jti);

    return new RefreshTokenInfo(token, jti, expiryDate);
  }

  public Claims parseAndValidateAccessToken(String token) {
    return parseAndValidateClaims(token, TOKEN_TYPE_ACCESS);
  }

  public Claims parseAndValidateRefreshToken(String token) {
    return parseAndValidateClaims(token, TOKEN_TYPE_REFRESH);
  }

  public String getTruncatedJti(String jti) {
    if (jti == null || jti.isEmpty()) {
      return "...";
    }
    int length = Math.min(JTI_LOG_PREFIX_LENGTH, jti.length());
    return jti.substring(0, length) + "...";
  }

  private String generateToken(User user, String tokenType, Instant issuedAt, long expirationMs) {
    return generateToken(user, tokenType, issuedAt, expirationMs, UUID.randomUUID().toString());
  }

  private String generateToken(
      User user, String tokenType, Instant issuedAt, long expirationMs, String jti) {
    validateUser(user);
    Instant expiryDate = issuedAt.plusMillis(expirationMs);

    return Jwts.builder()
        .id(jti)
        .subject(user.getId().toString())
        .claim(TOKEN_TYPE_CLAIM, tokenType)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiryDate))
        .signWith(signingKey)
        .compact();
  }

  private Claims parseAndValidateClaims(String token, String expectedTokenType) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .require(TOKEN_TYPE_CLAIM, expectedTokenType)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private void validateUser(User user) {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null");
    }
    if (user.getId() == null) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
  }
}
