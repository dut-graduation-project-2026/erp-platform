package com.dut.erp.config;

import com.dut.erp.config.properties.CookieProperties;
import com.dut.erp.config.properties.CorsProperties;
import com.dut.erp.config.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
    value = {CorsProperties.class, CookieProperties.class, JwtProperties.class})
public class PropertiesConfig {}
