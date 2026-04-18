package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cookie")
public record CookieProperties(
    @NotNull(message = "Cookie secure flag is required") Boolean secure,
    @NotNull(message = "Cookie httpOnly flag is required") Boolean httpOnly,
    @NotNull(message = "Cookie sameSite flag is required") String sameSite) {}
