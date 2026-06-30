package com.dut.erp.util;

import com.dut.erp.config.properties.CookieProperties;
import com.dut.erp.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtils {

  private static final String ACCESS_TOKEN_COOKIE = "access_token";
  private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  private static final String REFRESH_TOKEN_PATH = "/api/v1/auth";
  private static final String LEGACY_REFRESH_TOKEN_PATH = "/";
  private static final String ACCESS_TOKEN_PATH = "/";

  private final CookieProperties cookieProperties;
  private final JwtProperties jwtProperties;

  private ResponseCookie createCookie(String name, String value, String path, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(cookieProperties.httpOnly())
        .secure(cookieProperties.secure())
        .sameSite(cookieProperties.sameSite())
        .path(path)
        .maxAge(maxAge)
        .build();
  }

  public ResponseCookie createAccessTokenCookie(String token) {
    return createCookie(
        ACCESS_TOKEN_COOKIE,
        token,
        ACCESS_TOKEN_PATH,
        Duration.ofMillis(jwtProperties.accessTokenExpiration()));
  }

  public ResponseCookie createRefreshTokenCookie(String token) {
    return createCookie(
        REFRESH_TOKEN_COOKIE,
        token,
        REFRESH_TOKEN_PATH,
        Duration.ofMillis(jwtProperties.refreshTokenExpiration()));
  }

  public ResponseCookie clearAccessTokenCookie() {
    return createCookie(ACCESS_TOKEN_COOKIE, "", ACCESS_TOKEN_PATH, Duration.ZERO);
  }

  public ResponseCookie clearRefreshTokenCookie() {
    return createCookie(REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH, Duration.ZERO);
  }

  public ResponseCookie clearLegacyRefreshTokenCookie() {
    return createCookie(REFRESH_TOKEN_COOKIE, "", LEGACY_REFRESH_TOKEN_PATH, Duration.ZERO);
  }

  public void setAuthCookies(
      HttpServletResponse response, String accessToken, String refreshToken) {
    response.addHeader(HttpHeaders.SET_COOKIE, createAccessTokenCookie(accessToken).toString());
    // Clear legacy root-path cookie to avoid duplicate refresh_token entries.
    response.addHeader(HttpHeaders.SET_COOKIE, clearLegacyRefreshTokenCookie().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
  }

  public void clearAuthCookies(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, clearAccessTokenCookie().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, clearLegacyRefreshTokenCookie().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshTokenCookie().toString());
  }

  public Optional<String> extractCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    for (Cookie cookie : cookies) {
      if (cookieName.equals(cookie.getName())) {
        String value = cookie.getValue();
        if (value != null && !value.isBlank()) {
          return Optional.of(value);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> extractAccessToken(HttpServletRequest request) {
    return extractCookie(request, ACCESS_TOKEN_COOKIE);
  }

  public Optional<String> extractRefreshToken(HttpServletRequest request) {
    return extractCookie(request, REFRESH_TOKEN_COOKIE);
  }
}
