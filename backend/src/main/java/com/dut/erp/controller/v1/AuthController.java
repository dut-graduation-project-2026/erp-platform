package com.dut.erp.controller.v1;

import com.dut.erp.dto.jwt.TokenPair;
import com.dut.erp.dto.request.LoginRequest;
import com.dut.erp.dto.request.RegisterRequest;
import com.dut.erp.dto.response.AuthResponse;
import com.dut.erp.dto.response.UserResponse;
import com.dut.erp.service.AuthenticationService;
import com.dut.erp.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationService authenticationService;
  private final CookieUtils cookieUtils;

  @PostMapping("/login")
  public ResponseEntity<UserResponse> loginWithEmailAndPassword(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authenticationService.login(request);
    setAuthCookies(response, authResponse.tokens());
    return ResponseEntity.ok(authResponse.user());
  }

  @PostMapping("/refresh")
  public ResponseEntity<String> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    TokenPair newTokens = authenticationService.refreshToken(request);
    setAuthCookies(response, newTokens);
    return ResponseEntity.ok("Token refreshed successfully.");
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
    authenticationService.logout(request);
    cookieUtils.clearAuthCookies(response);
    return ResponseEntity.ok("Logged out successfully.");
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authenticationService.register(request);
    setAuthCookies(response, authResponse.tokens());
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse.user());
  }

  private void setAuthCookies(HttpServletResponse response, TokenPair tokens) {
    cookieUtils.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
  }
}
