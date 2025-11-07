package com.example.jammoney.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    @GetMapping("/redis-test")
    public String testRedis() {
        try {
            redisTemplate.opsForValue().set("ping", "pong", Duration.ofSeconds(30));
            String val = redisTemplate.opsForValue().get("ping");
            return "Redis OK - Value: " + val;
        } catch (Exception e) {
            e.printStackTrace();
            return "Redis ERROR - " + e.getMessage();
        }
    }
}
