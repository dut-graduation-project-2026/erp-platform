package com.dut.erp.config;

import com.dut.erp.config.properties.RedisProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {RedisProperties.class})
public class RedisCacheConfig {
  private static final Long CACHE_EXPIRATION_MINUTES = 30L;

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.WRAPPER_ARRAY);
    return mapper;
  }

  @Bean
  public RedisTemplate<String, Object> redisCacheTemplate(
      LettuceConnectionFactory redisConnectionFactory,
      ObjectMapper redisObjectMapper) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);

    // RedisSerializer.java(objectMapper) is the non-deprecated replacement
    RedisSerializer<Object> serializer = RedisSerializer.java();
    // Use JSON serializer with our configured ObjectMapper
    RedisSerializer<Object> jsonSerializer = (RedisSerializer<Object>) RedisSerializer.json();

    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(jsonSerializer);
    template.setValueSerializer(jsonSerializer);
    return template;
  }

  @Bean
  public CacheManager cacheManager(
      RedisConnectionFactory factory,
      ObjectMapper redisObjectMapper) {

    // Pass the configured ObjectMapper directly to RedisSerializer.json()
    RedisSerializer<Object> jsonSerializer = (RedisSerializer<Object>) RedisSerializer.json(redisObjectMapper);

    RedisCacheConfiguration redisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(CACHE_EXPIRATION_MINUTES))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

    return RedisCacheManager.builder(factory)
        .cacheDefaults(redisCacheConfiguration)
        .build();
  }
}
