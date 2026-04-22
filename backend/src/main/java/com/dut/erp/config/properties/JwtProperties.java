package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    @NotBlank(message = "JWT secret is required")
    @Size(min = 32, message = "JWT secret must be at least 32 characters long to be secure")
    String secret,

    @NotNull(message = "Access token expiration is required")
    @Positive(message = "Access token expiration must be positive")
    Long accessTokenExpiration,

    @NotNull(message = "Refresh token expiration is required")
    @Positive(message = "Refresh token expiration must be positive")
    Long refreshTokenExpiration) {}
