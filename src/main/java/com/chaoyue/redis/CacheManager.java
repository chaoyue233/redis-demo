package com.chaoyue.redis;

import java.io.Serializable;

/**
 * 抽象的缓存管理接口
 */
public interface CacheManager {

    /**
     * 保存至缓存 byte[]
     *
     * @param key     key
     * @param value   value
     * @param timeout 单位秒
     */
    void set(String key, byte[] value, long timeout) throws Exception;

    /**
     * 保存至缓存 byte[]
     *
     * @param key   key
     * @param value value
     */
    void set(String key, byte[] value) throws Exception;

    /**
     * 保存至缓存 object 序列化
     *
     * @param key     key
     * @param object  value
     * @param timeout 单位秒
     */
    void set(final String key, final Serializable object, final long timeout) throws Exception;

    /**
     * 保存至缓存 object 序列化
     */
    void set(final String key, final Serializable object) throws Exception;

    /**
     * 保存至缓存 String类型
     *
     * @param key     key
     * @param value   value
     * @param timeout 单位秒
     */
    void set(String key, String value, long timeout) throws Exception;

    /**
     * 保存至缓存 String类型
     */
    void set(String key, String value) throws Exception;

    /**
     * 从缓存中读取
     */
    Object get(String key) throws Exception;

    /**
     * 从缓存中读取 String返回值
     */
    String getString(String key) throws Exception;

    /**
     * 从缓存中读取 到固定的对象
     */
    <T> T get(final String key, Class<T> t) throws Exception;

    /**
     * 从缓存中移除
     */
    long del(String key) throws Exception;

}
