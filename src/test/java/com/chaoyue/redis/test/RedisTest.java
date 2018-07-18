package com.chaoyue.redis.test;

import com.chaoyue.redis.DefaultRedisCacheManager;
import com.chaoyue.redis.RedisCache;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisTest extends AbstractSpringContextTest {
    @Autowired
    private DefaultRedisCacheManager redisCacheManager;

    @Test
    public void putTest() {
        RedisCache redisCache = redisCacheManager.getRedisCache();

        String key = "chaoyue_string";
        String value = "chaoyue_string_value1";
        redisCache.putString(key, value, 1000);
        String result = redisCache.getString(key);
        System.out.println(result);
    }

    @Test
    public void deleteTest() {
        RedisCache redisCache = redisCacheManager.getRedisCache();

        String key = "chaoyue_string";
        redisCache.delete(key);
        String result = redisCache.getString(key);
        System.out.println(result);
    }
}
