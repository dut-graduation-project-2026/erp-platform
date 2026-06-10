package com.dut.erp.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dut.erp.dto.request.ForgotPasswordRequest;
import com.dut.erp.dto.request.ResetPasswordRequest;
import com.dut.erp.dto.request.SendMailRequest;
import com.dut.erp.entity.PasswordResetToken;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.InvalidatedAccessTokenRepository;
import com.dut.erp.repository.PasswordResetTokenRepository;
import com.dut.erp.repository.RefreshTokenRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.MailSenderService;
import com.dut.erp.service.MailTemplateService;
import com.dut.erp.util.CookieUtils;
import com.dut.erp.util.JwtUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private JwtUtils jwtUtils;
  @Mock private UserMapper userMapper;
  @Mock private CookieUtils cookieUtils;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private MailSenderService mailSenderService;
  @Mock private MailTemplateService mailTemplateService;

  @InjectMocks private AuthenticationServiceImpl authenticationService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .email("mienhin123@gmail.com")
        .password("oldHashedPassword")
        .firstName("John")
        .lastName("Doe")
        .build();
  }

  @Test
  void sendForgotPasswordEmail_UserExists_SendsEmail() {
    // Arrange
    ForgotPasswordRequest request = new ForgotPasswordRequest("mienhin123@gmail.com");
    when(userRepository.findByEmail("mienhin123@gmail.com")).thenReturn(Optional.of(testUser));
    when(mailTemplateService.generateForgotPasswordEmailContent(anyString(), anyString(), any(Instant.class)))
        .thenReturn("html-content");

    // Act
    authenticationService.sendForgotPasswordEmail(request);

    // Assert
    verify(passwordResetTokenRepository).deleteByUser(testUser);
    verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    verify(mailSenderService).sendMail(any(SendMailRequest.class));
  }

  @Test
  void sendForgotPasswordEmail_UserDoesNotExist_ThrowsException() {
    // Arrange
    ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@gmail.com");
    when(userRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        ResourceNotFoundException.class,
        () -> authenticationService.sendForgotPasswordEmail(request));
    verifyNoInteractions(passwordResetTokenRepository, mailSenderService);
  }

  @Test
  void resetPassword_ValidToken_UpdatesPassword() {
    // Arrange
    String token = "valid-token";
    ResetPasswordRequest request = new ResetPasswordRequest(token, "newPassword123");
    PasswordResetToken resetToken = PasswordResetToken.builder()
        .user(testUser)
        .token(token)
        .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
        .build();

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
    when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

    // Act
    authenticationService.resetPassword(request);

    // Assert
    verify(passwordEncoder).encode("newPassword123");
    verify(userRepository).save(testUser);
    verify(passwordResetTokenRepository).deleteByUser(testUser);
  }

  @Test
  void resetPassword_TokenExpired_ThrowsException() {
    // Arrange
    String token = "expired-token";
    ResetPasswordRequest request = new ResetPasswordRequest(token, "newPassword123");
    PasswordResetToken resetToken = PasswordResetToken.builder()
        .user(testUser)
        .token(token)
        .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
        .build();

    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

    // Act & Assert
    assertThrows(BadRequestException.class, () -> authenticationService.resetPassword(request));
    verify(passwordResetTokenRepository).delete(resetToken);
    verifyNoInteractions(passwordEncoder, userRepository);
  }

  @Test
  void resetPassword_TokenNotFound_ThrowsException() {
    // Arrange
    String token = "invalid-token";
    ResetPasswordRequest request = new ResetPasswordRequest(token, "newPassword123");
    when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BadRequestException.class, () -> authenticationService.resetPassword(request));
    verifyNoInteractions(passwordEncoder, userRepository);
  }
}
