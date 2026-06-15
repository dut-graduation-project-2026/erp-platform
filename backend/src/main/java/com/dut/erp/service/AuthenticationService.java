package com.dut.erp.service;

import com.dut.erp.dto.jwt.TokenPair;
import com.dut.erp.dto.request.ForgotPasswordRequest;
import com.dut.erp.dto.request.LoginRequest;
import com.dut.erp.dto.request.RegisterRequest;
import com.dut.erp.dto.request.ResetPasswordRequest;
import com.dut.erp.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
  AuthResponse login(LoginRequest request);

  AuthResponse register(RegisterRequest request);

  TokenPair refreshToken(HttpServletRequest request);

  void logout(HttpServletRequest request);

  void sendForgotPasswordEmail(ForgotPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);

  void validateResetToken(String token);
}
