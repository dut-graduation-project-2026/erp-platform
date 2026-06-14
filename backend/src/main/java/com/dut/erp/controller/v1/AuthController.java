package com.dut.erp.controller.v1;

import com.dut.erp.dto.jwt.TokenPair;
import com.dut.erp.dto.request.ForgotPasswordRequest;
import com.dut.erp.dto.request.LoginRequest;
import com.dut.erp.dto.request.RegisterRequest;
import com.dut.erp.dto.request.ResetPasswordRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication-related API endpoints.
 * Provides endpoints for user login, registration, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationService authenticationService;
  private final CookieUtils cookieUtils;

  /**
   * Authenticates a user with email and password.
   *
   * <p>Upon successful login, access and refresh tokens are set as HTTP-only cookies in the
   * response. The authenticated user's details are returned in the response body.
   *
   * @param request the login request containing email and password
   * @param response the HTTP response used to set authentication cookies
   * @return a ResponseEntity containing the authenticated user's data (UserResponse)
   */
  @PostMapping("/login")
  public ResponseEntity<UserResponse> loginWithEmailAndPassword(
      @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authenticationService.login(request);
    setAuthCookies(response, authResponse.tokens());
    return ResponseEntity.ok(authResponse.user());
  }

  /**
   * Refreshes the authentication tokens using a valid refresh token.
   *
   * <p>The refresh token is extracted from the request cookies. If valid, a new token pair (access
   * + refresh) is generated and set as cookies.
   *
   * @param request the HTTP request containing the refresh token cookie
   * @param response the HTTP response used to set the new token cookies
   * @return a success message indicating that the token was refreshed
   */
  @PostMapping("/refresh")
  public ResponseEntity<String> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    TokenPair newTokens = authenticationService.refreshToken(request);
    setAuthCookies(response, newTokens);
    return ResponseEntity.ok("Token refreshed successfully.");
  }

  /**
   * Logs out the current user.
   *
   * <p>Invalidates the refresh token on the server side and clears the authentication cookies from
   * the client's browser.
   *
   * @param request the HTTP request containing the refresh token to invalidate
   * @param response the HTTP response used to clear authentication cookies
   * @return a success message indicating successful logout
   */
  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
    authenticationService.logout(request);
    cookieUtils.clearAuthCookies(response);
    return ResponseEntity.ok("Logged out successfully.");
  }

  /**
   * Registers a new user account.
   *
   * <p>Creates a new user with the provided registration data. After successful registration, the
   * user is automatically authenticated and access/refresh tokens are set as cookies.
   *
   * @param request the registration request containing user details (e.g., email, password, name)
   * @param response the HTTP response used to set authentication cookies
   * @return a ResponseEntity with HTTP status 201 (Created) containing the newly registered user's
   *     data
   */
  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(
      @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
    AuthResponse authResponse = authenticationService.register(request);
    setAuthCookies(response, authResponse.tokens());
    return ResponseEntity.status(HttpStatus.CREATED).body(authResponse.user());
  }

  /**
   * Sends a password reset link to the user's email if they forgot their password.
   *
   * @param request the request containing the user's email
   * @return a success message indicating that the reset email was sent
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<String> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    authenticationService.sendForgotPasswordEmail(request);
    return ResponseEntity.ok("Password reset email sent successfully.");
  }

  /**
   * Resets the user's password using the validation token.
   *
   * @param request the request containing the token and new password
   * @return a success message indicating successful password reset
   */
  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    authenticationService.resetPassword(request);
    return ResponseEntity.ok("Password has been reset successfully.");
  }

  /**
   * Validates a password reset token.
   *
   * @param token the token to validate
   * @return a success message indicating successful validation
   */
  @GetMapping("/reset-password/validate")
  public ResponseEntity<String> validateResetToken(@RequestParam String token) {
    authenticationService.validateResetToken(token);
    return ResponseEntity.ok("Token is valid.");
  }

  /**
   * Utility helper to set access and refresh tokens as cookies in the HTTP response.
   *
   * @param response the HTTP response
   * @param tokens the generated TokenPair containing access and refresh tokens
   */
  private void setAuthCookies(HttpServletResponse response, TokenPair tokens) {
    cookieUtils.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
  }
}
