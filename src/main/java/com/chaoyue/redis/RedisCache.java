package com.chaoyue.redis;


import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class RedisCache {


    private DefaultRedisCacheManager redisManager;

    private String prefix = "chaoyue:";

    public RedisCache(DefaultRedisCacheManager redisManager) {
        super();
        this.redisManager = redisManager;
    }

    public RedisCache(DefaultRedisCacheManager redisManager, String prefix) {
        super();
        this.redisManager = redisManager;
        this.prefix = prefix;
    }

    @SuppressWarnings("Duplicates")
    public Serializable put(String key, Serializable value, int seconds) {
        try {
            if (seconds > 0) {
                redisManager.set(prefix + key, value, seconds);
            } else {
                redisManager.set(prefix + key, value);
            }
        } catch (Exception e) {
            log.error("fail to put.", e);
        }
        return value;
    }

    @SuppressWarnings("Duplicates")
    public String putString(String key, String value, int seconds) {
        try {
            if (seconds > 0) {
                redisManager.set(prefix + key, value, seconds);
            } else {
                redisManager.set(prefix + key, value);
            }
        } catch (Exception e) {
            log.error("fail to put.", e);
        }
        return value;
    }

    public long increment(String key, Long delta, int seconds) {
        long count = 0;
        try {
            if (delta == null) {
                delta = 1L;
            }
            if (seconds <= 0) {
                seconds = 1;
            }
            count = redisManager.increment(key, delta);
            redisManager.expire(key, seconds);
        } catch (Exception e) {
            log.error("fail to increment", e);
        }
        return count;
    }

    public String getString(String key) {
        try {

            return redisManager.getString(prefix + key);
        } catch (Exception e) {
            log.error("fail to get.", e);
        }
        return null;
    }

    public <T> T get(String key, Class<T> t) {
        try {

            return redisManager.get(prefix + key, t);
        } catch (Exception e) {
            log.error("fail to get.", e);
        }
        return null;
    }

    public void delete(String key) {
        try {
            redisManager.del(key);
        } catch (Exception e) {
            log.error("delete redis error key : " + key);
        }
    }

}
