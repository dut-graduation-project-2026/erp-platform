package com.dut.erp.security;

import com.dut.erp.constant.RequestAttributeKeys;
import com.dut.erp.entity.User;
import com.dut.erp.enums.ErrorCode;
import com.dut.erp.repository.InvalidatedAccessTokenRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.util.CookieUtils;
import com.dut.erp.util.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final CookieUtils cookieUtils;
  private final UserRepository userRepository;
  private final InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> accessToken = cookieUtils.extractAccessToken(request);
    if (accessToken.isEmpty()) {
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.MISSING_TOKEN);
    } else {
      accessToken
          .flatMap(token -> validateAndGetClaims(token, request))
          .filter(claims -> isTokenNotRevoked(claims.getId(), request))
          .flatMap(claims -> getValidUser(claims.getSubject(), request))
          .ifPresent(
              user -> {
                setAuthentication(request, user);
                log.debug(
                    "Authenticated user: id={}, path={}", user.getId(), request.getRequestURI());
              });
    }
    filterChain.doFilter(request, response);
  }

  private Optional<Claims> validateAndGetClaims(String token, HttpServletRequest request) {
    try {
      log.debug("Validating JWT from path={}", request.getRequestURI());
      return Optional.of(jwtUtils.parseAndValidateAccessToken(token));
    } catch (ExpiredJwtException e) {
      log.debug("Expired access token on path={}", request.getRequestURI());
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.TOKEN_EXPIRED);
      return Optional.empty();
    } catch (JwtException e) {
      log.warn("Malformed JWT on path={}, reason={}", request.getRequestURI(), e.getMessage());
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return Optional.empty();
    }
  }

  private boolean isTokenNotRevoked(String jti, HttpServletRequest request) {
    if (invalidatedAccessTokenRepository.existsById(jti)) {
      log.info("Rejected revoked token: jti={}", jwtUtils.getTruncatedJti(jti));
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return false;
    }
    return true;
  }

  private Optional<User> getValidUser(String userIdString, HttpServletRequest request) {
    try {
      UUID userId = UUID.fromString(userIdString);
      Optional<User> userOpt = userRepository.findByIdWithRolesAndOrganizations(userId);
      if (userOpt.isEmpty()) {
        log.warn("No user found for valid token: userId={}", userIdString);
        request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      }
      return userOpt;
    } catch (IllegalArgumentException e) {
      log.warn("Corrupt subject claim in token: subject={}", userIdString);
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return Optional.empty();
    }
  }

  private void setAuthentication(HttpServletRequest request, User user) {
    CustomUserDetails userDetails = CustomUserDetails.build(user);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
