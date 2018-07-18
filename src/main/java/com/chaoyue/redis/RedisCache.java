package com.chaoyue.redis;


import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisCache {


    private DefaultRedisCacheManager redisManager;

    private String prefix = "chaoyue:";

    RedisCache(DefaultRedisCacheManager redisManager) {
        super();
        this.redisManager = redisManager;
    }

//    RedisCache(DefaultRedisCacheManager redisManager, String prefix) {
//        super();
//        this.redisManager = redisManager;
//        this.prefix = prefix;
//    }

    /**
     * 添加数据到redis
     *
     * @param key     key
     * @param value   序列化的value
     * @param seconds 过期时间 如果<0 则永不过期
     * @return 序列化的value
     */
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

    /**
     * @param key     key
     * @param value   String 类型的value
     * @param seconds 过期时间 如果<0 则用不过去
     * @return String 类型的value
     */
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

    /**
     * 根据key 获取相应的值
     *
     * @param key key
     * @param t   转换的类型
     * @return 类型装换后的值
     */
    public <T> T get(String key, Class<T> t) {
        try {

            return redisManager.get(prefix + key, t);
        } catch (Exception e) {
            log.error("fail to get.", e);
        }
        return null;
    }

    /**
     * 根据key 获取String类型的值
     *
     * @param key key
     * @return String类型的值
     */
    public String getString(String key) {
        try {

            return redisManager.getString(prefix + key);
        } catch (Exception e) {
            log.error("fail to get.", e);
        }
        return null;
    }

    /**
     * 根据key 删除
     *
     * @param key key
     */
    public void delete(String key) {
        try {
            redisManager.del(key);
        } catch (Exception e) {
            log.error("delete redis error key : " + key);
        }
    }

    /**
     * 为 key 添加 delta 的计数 并返回 key当前的计数
     * 该方法主要用来给key进行短时间的应用计数，并在业务逻辑上控制加锁
     *
     * @param key   key
     * @param delta 增量 一般为1
     * @param ms    去除时间毫秒值
     * @return 当前key的count值
     */
    public long increment(String key, Long delta, long ms) {
        long count = 0;
        try {
            if (delta == null) {
                delta = 1L;
            }
            if (ms <= 0) {
                // 默认为100毫秒失效
                ms = 100;
            }
            count = redisManager.increment(key, delta);
            redisManager.expire(key, ms, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("fail to increment", e);
        }
        return count;
    }

    /**
     * 向渠道发布消息
     *
     * @param channel 渠道
     * @param value   发布的消息内容
     */
    public void publish(String channel, String value) {
        try {
            redisManager.publish(channel, value);
        } catch (Exception e) {
            log.error("publish redis error channel : " + channel + " value " + value);
        }
    }

}
