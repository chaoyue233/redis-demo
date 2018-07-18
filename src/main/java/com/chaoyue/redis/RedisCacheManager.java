package com.chaoyue.redis;

import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 的缓存管理接口
 */
public interface RedisCacheManager extends CacheManager {

    /**
     * 模糊查询，获取匹配的key
     *
     * @param pattern 匹配的正则表达式
     * @return keys
     */
    Set<String> keys(final String pattern) throws Exception;

    /**
     * 添加
     *
     * @param key
     * @param fieldKey
     * @param value
     * @return result
     */
    boolean hSet(String key, String fieldKey, Serializable value) throws Exception;

    /**
     * 多字段hashset,value为序列化对象
     *
     * @param key
     * @param fieldMap
     * @return result
     */
    <T extends Serializable> void hMSet(String key, Map<String, T> fieldMap) throws Exception;

    /**
     * 从缓存中的map中获取值,value为序列化对象
     *
     * @param key
     * @param fieldKey
     * @return value
     */
    Object hGet(String key, String fieldKey) throws Exception;

    /**
     * hash删除
     *
     * @param key
     * @param fields
     * @return result
     */
    long hDel(String key, String... fieldKeys) throws Exception;

    /**
     * 获取hashMap所有对象
     *
     * @param key
     * @param clazz
     * @return map
     */
    <T> Map<String, T> hGetAll(String key, Class<T> clazz) throws Exception;

    /**
     * 队列移出
     *
     * @param key
     * @return
     * @throws Exception
     */
    byte[] leftPop(String key) throws Exception;

    /**
     * 加入队列
     *
     * @param key
     * @param value
     * @throws Exception
     */
    boolean rightPush(String key, byte[] value) throws Exception;

    /**
     * 设置超时时间
     *
     * @param key
     * @param timeout
     * @param unit
     * @return
     * @throws Exception
     */
    boolean expire(String key, long timeout, TimeUnit unit) throws Exception;

    /**
     * 设置超时时间 单位秒
     *
     * @param key
     * @param timeout
     * @return
     * @throws Exception
     */
    boolean expire(String key, long timeout) throws Exception;

    /**
     * 发布消息.
     *
     * @param channel
     * @param msg
     * @return
     * @throws Exception
     */
    long publish(String channel, String msg) throws Exception;

    /**
     * set获取长度
     *
     * @param key
     * @return
     * @throws Exception
     */
    long zSize(String key) throws Exception;

    /**
     * set统计排序后的数据在min和max之间的数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     * @throws Exception
     */
    long zCount(String key, double min, double max) throws Exception;

    Set<byte[]> zRangeByScore(final String key, final double min, final double max, final long offset,
                              final long count) throws Exception;

    /**
     * set移除
     *
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    long zRemove(String key, byte[]... value) throws Exception;

    /**
     * set添加或更新key的值
     *
     * @param key
     * @param value
     * @param score
     * @return
     * @throws Exception
     */
    boolean zAdd(String key, byte[] value, double score) throws Exception;

    /**
     * 获取并赋值
     *
     * @param key
     * @param values
     * @param time
     * @param timeUnit
     * @return
     * @throws Exception
     */
    byte[] getSet(String key, byte[] values, long time, TimeUnit timeUnit) throws Exception;

    /**
     * 获取并赋值
     *
     * @param key
     * @param values
     * @param time   单位秒
     * @return
     * @throws Exception
     */
    byte[] getSet(String key, byte[] values, long time) throws Exception;

    /**
     * 获取值在min到max直接的key，且倒序排列
     *
     * @param key
     * @param min
     * @param max
     * @return
     * @throws Exception
     */
    Set<byte[]> zRevRangeByScore(String key, double min, double max) throws Exception;

    /**
     * 获取score在min到max直接的key，且倒序排列. offset count参数类似于 mysql 的 limit offset,count
     *
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return
     * @throws Exception
     */
    Set<byte[]> zRevRangeByScore(String key, double min, double max, long offset, long count)
            throws Exception;

    /**
     * 取score介于min到max之间的key
     *
     * @param key
     * @param min
     * @param max
     * @return
     * @throws Exception
     */
    Set<byte[]> zRangeByScore(String key, final double min, final double max) throws Exception;

    /**
     * 删除多个key
     *
     * @param keys
     * @throws Exception
     */
    void delete(List<String> keys) throws Exception;

    /**
     * 删除score介于min到max之间的key
     *
     * @param key
     * @param min
     * @param max
     * @return
     * @throws Exception
     */
    long zRemoveRangeByScore(String key, double min, double max) throws Exception;

    /**
     * 计算下一个递增key值
     *
     * @param key key
     * @param delta 增量
     * @return 递增值
     */
    long increment(String key, Long delta) throws Exception;

    /**
     * 获取队列长度
     *
     * @param key key
     * @return list类型数据长度
     */
    long listSize(String key) throws Exception;

    /**
     * 获取DefaultSerializer
     *
     * @return JdkSerializationRedisSerializer
     */
    RedisSerializer<Object> getDefRedisSerializer();

    /**
     * 获取StringSerializer
     *
     * @return StringSerializer
     */
    RedisSerializer<String> getStringSerializer();

    /**
     * 获取剩余操作时间
     *
     * @param key key
     * @return 剩余时间
     */
    long ttl(String key) throws Exception;
}
