package com.zj.service;

import com.zj.UserCenterApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest(classes = UserCenterApplication.class)
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){

    }
}
