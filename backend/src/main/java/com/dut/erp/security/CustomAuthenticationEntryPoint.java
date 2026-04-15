package com.dut.erp.security;

import com.dut.erp.constant.RequestAttributeKeys;
import com.dut.erp.enums.ErrorCode;
import com.dut.erp.exception.UnauthorizedAccessException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver resolver;

  public CustomAuthenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    ErrorCode errorCode = (ErrorCode) request.getAttribute(RequestAttributeKeys.JWT_ERROR);
    Exception exceptionToResolve = createExceptionFromErrorCode(errorCode, authException);

    resolver.resolveException(request, response, null, exceptionToResolve);
  }

  private Exception createExceptionFromErrorCode(
      ErrorCode errorCode, AuthenticationException fallback) {
    if (errorCode == null) {
      return fallback;
    }

    return switch (errorCode) {
      case TOKEN_EXPIRED -> new UnauthorizedAccessException("Authentication token has expired");
      case INVALID_TOKEN -> new UnauthorizedAccessException("Authentication token is invalid");
      case USER_NOT_FOUND ->
          new UnauthorizedAccessException("User not found for provided authentication token");
      default -> fallback;
    };
  }
}
