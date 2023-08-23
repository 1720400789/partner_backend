package com.zj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis的配置类，源码中表示redis会默认配置一个java原生的序列化器，这使得k-v结构在存入redis库时，
 * k会序列化上一些乱码的前缀，所以我们要自定义一个RedisTemplate
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());//return StringRedisSerializer.UTF_8配置一个string的序列化器
        return redisTemplate;
    }
}