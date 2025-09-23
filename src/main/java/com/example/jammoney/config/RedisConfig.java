package com.example.jammoney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }


    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> t = new RedisTemplate<>();
        t.setConnectionFactory(cf);
        StringRedisSerializer s = new StringRedisSerializer();
        t.setKeySerializer(s);
        t.setValueSerializer(s);
        t.setHashKeySerializer(s);
        t.setHashValueSerializer(s);
        t.afterPropertiesSet();
        return t;
    }
}