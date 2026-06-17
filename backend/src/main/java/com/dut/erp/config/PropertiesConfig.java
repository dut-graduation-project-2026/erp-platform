package com.dut.erp.config;

import com.dut.erp.config.properties.CookieProperties;
import com.dut.erp.config.properties.CorsProperties;
import com.dut.erp.config.properties.EmailProperties;
import com.dut.erp.config.properties.JwtProperties;
import com.dut.erp.config.properties.SystemDomainProperties;
import com.dut.erp.config.properties.GeoapifyProperties;
import com.dut.erp.config.properties.CloudinaryProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(
    value = {
      CorsProperties.class,
      CookieProperties.class,
      SystemDomainProperties.class,
      JwtProperties.class,
      EmailProperties.class,
      GeoapifyProperties.class,
      CloudinaryProperties.class
    })
public class PropertiesConfig {}
