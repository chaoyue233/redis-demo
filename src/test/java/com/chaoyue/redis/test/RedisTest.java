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

    @Test
    public void sendMessageTest() {
        RedisCache redisCache = redisCacheManager.getRedisCache();

        String channel = "chaoyue_channel";
        String value = "chaoyue_value_1";
        redisCache.publish(channel, value);
        // 需要通过客户端接受消息来验证是否发送成功
    }

    @Test
    public void incrementTest() throws InterruptedException {
        RedisCache redisCache = redisCacheManager.getRedisCache();
        String key = "chaoyue_string";
        for (int i = 0; i < 10; i++) {
            System.out.println(redisCache.increment(key, 1L, 100));
        }
        redisCache.increment(key, 1L, 100);
        System.out.println("wait to clear");
        Thread.sleep(1000);
        System.out.println(redisCache.increment(key, 1L, 100));
    }
}
