package com.dut.erp.service.impl;

import com.dut.erp.dto.jwt.RefreshTokenInfo;
import com.dut.erp.dto.jwt.TokenPair;
import com.dut.erp.dto.request.LoginRequest;
import com.dut.erp.dto.request.RegisterRequest;
import com.dut.erp.dto.response.AuthResponse;
import com.dut.erp.dto.request.ForgotPasswordRequest;
import com.dut.erp.dto.request.ResetPasswordRequest;
import com.dut.erp.dto.request.SendMailRequest;
import com.dut.erp.entity.PasswordResetToken;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.exception.UnauthorizedAccessException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.InvalidatedAccessTokenRepository;
import com.dut.erp.repository.PasswordResetTokenRepository;
import com.dut.erp.repository.RefreshTokenRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.AuthenticationService;
import com.dut.erp.service.MailSenderService;
import com.dut.erp.service.MailTemplateService;
import java.time.Duration;
import com.dut.erp.util.CookieUtils;
import com.dut.erp.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
  private final JwtUtils jwtUtils;
  private final UserMapper userMapper;
  private final CookieUtils cookieUtils;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final MailSenderService mailSenderService;
  private final MailTemplateService mailTemplateService;

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt for email: {}", request.email());

    User user = userRepository.findByEmail(request.email()).orElse(null);

    if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
      if (user == null) {
        log.warn("Login failed: user not found for email: {}", request.email());
      } else {
        log.warn("Login failed: incorrect password for email: {}", request.email());
      }
      throw new UnauthorizedAccessException("Invalid email or password. Please try again.");
    }

    TokenPair tokens = generateTokens(user);

    log.info("Login successful for userId: {}, email: {}", user.getId(), user.getEmail());
    return new AuthResponse(userMapper.toUserResponse(user), tokens);
  }

  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    log.info("Register attempt for email: {}", request.email());

    boolean emailExists = userRepository.existsByEmail(request.email());

    if (emailExists) {
      log.warn("Registration failed: email already in use: {}", request.email());
      throw new ResourceAlreadyExistsException(
          "An account with this email address already exists. Please log in or use a different email.");
    }

    User user =
        User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .build();

    user = userRepository.save(user);

    TokenPair tokens = generateTokens(user);

    log.info("User registered successfully with id: {}, email: {}", user.getId(), user.getEmail());
    return new AuthResponse(userMapper.toUserResponse(user), tokens);
  }

  @Override
  @Transactional
  public void logout(HttpServletRequest request) {
    log.info("Logout request received");

    String accessToken = cookieUtils.extractAccessToken(request).orElse(null);
    String refreshToken = cookieUtils.extractRefreshToken(request).orElse(null);

    if (accessToken != null && !accessToken.isBlank()) {
      try {
        Claims accessTokenClaims = extractAccessTokenClaims(accessToken);
        invalidateAccessToken(accessTokenClaims);
        log.debug("Access token invalidated successfully");
      } catch (UnauthorizedAccessException e) {
        log.debug("Access token invalidation skipped during logout: {}", e.getMessage());
      }
    }

    if (refreshToken != null && !refreshToken.isBlank()) {
      try {
        Claims refreshTokenClaims = extractRefreshTokenClaims(refreshToken);
        revokeRefreshToken(refreshTokenClaims);
        log.debug("Refresh token revoked successfully");
      } catch (UnauthorizedAccessException e) {
        log.debug("Refresh token revocation skipped during logout: {}", e.getMessage());
      }
    }

    log.info("Logout completed");
  }

  @Override
  @Transactional
  public TokenPair refreshToken(HttpServletRequest request) {
    log.info("Refresh token request received");

    String refreshToken =
        cookieUtils
            .extractRefreshToken(request)
            .orElseThrow(
                () -> {
                  log.warn("Refresh token missing in request");
                  return new UnauthorizedAccessException("Refresh token is missing.");
                });

    TokenPair newTokens = refreshToken(refreshToken);

    log.info("Token refreshed successfully");
    return newTokens;
  }

  private TokenPair refreshToken(String refreshToken) {
    log.debug("Processing refresh token");

    Claims claims = extractRefreshTokenClaims(refreshToken);
    User user = getUserFromClaims(claims);

    revokeRefreshToken(claims);

    log.debug("Refresh token revoked and new tokens generated for userId: {}", user.getId());
    return generateTokens(user);
  }

  private User getUserFromClaims(Claims claims) {
    UUID userId = UUID.fromString(claims.getSubject());

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("User not found for id in refresh token claims: {}", userId);
              return new UnauthorizedAccessException("Invalid refresh token.");
            });
  }

  private void invalidateAccessToken(Claims claims) {
    String jti = claims.getId();
    Instant expiresAt = claims.getExpiration().toInstant();

    int inserted = invalidatedAccessTokenRepository.insertIgnoreJti(jti, expiresAt);

    if (inserted == 0) {
      log.warn("Access token already used or revoked - jti: {}", jwtUtils.getTruncatedJti(jti));
    } else {
      log.debug("Access token invalidated - jti: {}", jwtUtils.getTruncatedJti(jti));
    }
  }

  private void revokeRefreshToken(Claims claims) {
    String jti = claims.getId();

    int deleted = refreshTokenRepository.deleteByJti(jti);

    if (deleted == 0) {
      log.warn("Refresh token already used or revoked - jti: {}", jwtUtils.getTruncatedJti(jti));
      throw new UnauthorizedAccessException("Your session has expired. Please log in again.");
    } else {
      log.debug("Refresh token revoked - jti: {}", jwtUtils.getTruncatedJti(jti));
    }
  }

  private Claims extractAccessTokenClaims(String token) {
    return extractClaims(token, jwtUtils::parseAndValidateAccessToken, "Access");
  }

  private Claims extractRefreshTokenClaims(String token) {
    return extractClaims(token, jwtUtils::parseAndValidateRefreshToken, "Refresh");
  }

  private Claims extractClaims(String token, Function<String, Claims> parser, String tokenType) {
    try {
      return parser.apply(token);
    } catch (ExpiredJwtException e) {
      log.debug("{} token expired", tokenType);
      throw new UnauthorizedAccessException("Your authentication session has expired. Please log in again.");
    } catch (JwtException e) {
      log.warn("Invalid JWT {} token received", tokenType);
      throw new UnauthorizedAccessException("Your session is invalid or has expired. Please log in again.");
    }
  }

  private TokenPair generateTokens(User user) {
    Instant now = Instant.now();

    String accessToken = jwtUtils.generateAccessToken(user, now);
    RefreshTokenInfo refreshToken = jwtUtils.generateRefreshToken(user, now);

    refreshTokenRepository.save(refreshToken.toEntity(user));

    log.debug("Generated tokens for userId: {}", user.getId());

    return new TokenPair(accessToken, refreshToken.token());
  }

  @Override
  @Transactional
  public void sendForgotPasswordEmail(ForgotPasswordRequest request) {
    log.info("Password reset request received for email: {}", request.email());

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(
                () -> {
                  log.warn("Password reset failed: user not found for email: {}", request.email());
                  return new ResourceNotFoundException("User not found with email: " + request.email());
                });

    // Revoke existing reset tokens
    passwordResetTokenRepository.deleteByUser(user);

    // Create a new reset token
    String token = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plus(Duration.ofHours(1));

    PasswordResetToken resetToken =
        PasswordResetToken.builder()
            .user(user)
            .token(token)
            .expiresAt(expiresAt)
            .build();

    passwordResetTokenRepository.save(resetToken);

    // Generate email content
    String content = mailTemplateService.generateForgotPasswordEmailContent(user.getEmail(), token, expiresAt);

    SendMailRequest mailRequest =
        new SendMailRequest(
            user.getEmail(),
            "Reset Your Password - DUT ERP",
            content,
            true);

    // Send the email
    mailSenderService.sendMail(mailRequest);
    log.info("Password reset email sent to {}", user.getEmail());
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    log.info("Password reset attempt with token: {}", request.token());

    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(request.token())
            .orElseThrow(
                () -> {
                  log.warn("Password reset failed: token not found");
                  return new BadRequestException("The password reset link is invalid or has expired. Please request a new one.");
                });

    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      log.warn("Password reset failed: token expired");
      passwordResetTokenRepository.delete(resetToken);
      throw new BadRequestException("The password reset link is invalid or has expired. Please request a new one.");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    // Revoke token after successful use
    passwordResetTokenRepository.deleteByUser(user);
    log.info("Password reset successfully for user: {}", user.getEmail());
  }

  @Override
  @Transactional
  public void validateResetToken(String token) {
    log.info("Validating password reset token: {}", token);

    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(
                () -> {
                  log.warn("Token validation failed: token not found");
                  return new BadRequestException("Invalid or expired password reset token.");
                });

    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      log.warn("Token validation failed: token expired");
      passwordResetTokenRepository.delete(resetToken);
      throw new BadRequestException("Invalid or expired password reset token.");
    }
    log.info("Password reset token is valid");
  }
}
