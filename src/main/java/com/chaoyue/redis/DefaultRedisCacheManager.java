package com.chaoyue.redis;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.BinaryClient;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 基于 RedisTemplate 的扩展实现 处理对redis的基本操作
 * 使用内部类实现 doInRedis 操作 RedisConnection 去实现想要的功能
 * RedisTemplate 内部也是这样操作的，这边只是再抽象一层 方便进行统一的Cache层封装
 */
@Slf4j
public class DefaultRedisCacheManager implements RedisCacheManager {

    private RedisTemplate<String, Serializable> redisTemplate;

    private RedisSerializer<Object> defRedisSerializer;

    private int dbIndex;

    private RedisCache redisCache;

    /**
     * 设置 redisTemplate
     */
    public void setRedisTemplate(RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * @param defRedisSerializer the defRedisSerializer to set
     */
    public void setDefRedisSerializer(RedisSerializer<Object> defRedisSerializer) {
        this.defRedisSerializer = defRedisSerializer;
    }

    /**
     * 获取DefaultSerializer
     *
     * @return JdkSerializationRedisSerializer
     */
    public RedisSerializer<Object> getDefRedisSerializer() {
        if (defRedisSerializer == null) {
            return (JdkSerializationRedisSerializer) redisTemplate.getDefaultSerializer();
        }
        return defRedisSerializer;
    }

    /**
     * 获取StringSerializer
     *
     * @return StringSerializer
     */
    public RedisSerializer<String> getStringSerializer() {
        return redisTemplate.getStringSerializer();
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public Set<String> keys(final String pattern) throws Exception {
        return execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] patternBytes = getStringSerializer().serialize(pattern);
                Set<byte[]> keysBytes = connection.keys(patternBytes);

                if (keysBytes == null) {
                    return Collections.emptySet();
                }
                Set<String> keySet = new HashSet<String>(keysBytes.size());
                for (byte[] bytes : keysBytes) {
                    keySet.add(getStringSerializer().deserialize(bytes));
                }
                return keySet;
            }
        });

    }

    /**
     * 保存至缓存
     *
     * @param timeout 单位秒
     */
    public void set(final String key, final byte[] value, final long timeout) throws Exception {
        if (value != null) {
            execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    byte[] keyBytes = getStringSerializer().serialize(key);
                    connection.setEx(keyBytes, timeout, value);
                    return null;
                }

            });
        }
    }

    /**
     * 保存至缓存
     */
    public void set(final String key, final byte[] value) throws Exception {
        if (value != null) {
            execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    byte[] bytes = getStringSerializer().serialize(key);
                    connection.set(bytes, value);
                    return null;
                }
            });
        }
    }

    public void set(final String key, final Serializable object, final long timeout)
            throws Exception {
        this.set(key, getDefRedisSerializer().serialize(object), timeout);
    }

    public void set(final String key, final Serializable object) throws Exception {
        this.set(key, getDefRedisSerializer().serialize(object));
    }

    /**
     * 从缓存中读取
     */
    public Object get(final String key) throws Exception {
        return execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                byte[] valueBytes = connection.get(keyBytes);
                Object o = null;
                if (valueBytes != null) {
                    try {
                        o = getDefRedisSerializer().deserialize(valueBytes);
                    } catch (Exception e) {
                        // LOGGER.warn("不能反序列化，取原byte数组 key:" + key);
                        o = valueBytes;
                    }
                }
                return o;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String key, Class<T> t) throws Exception {
        Object object = this.get(key);
        if (object instanceof byte[]) {
            return JSON.parseObject(new String((byte[]) object), t);
        } else {
            return (T) object;
        }
    }

    /**
     * 从缓存中移除
     */
    public long del(final String key) throws Exception {
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                return connection.del(getStringSerializer().serialize(key));
            }
        });
    }

    @Override
    public boolean hSet(final String key, final String fieldKey, final Serializable value)
            throws Exception {
        if (value != null) {
            return execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    return connection.hSet(getStringSerializer().serialize(key),
                            getStringSerializer().serialize(fieldKey), getDefRedisSerializer().serialize(value));
                }
            });
        }
        return false;
    }

    @Override
    public <T extends Serializable> void hMSet(final String key, final Map<String, T> fieldMap)
            throws Exception {
        if (fieldMap != null && !fieldMap.isEmpty()) {
            execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    Map<byte[], byte[]> cacheValue = new HashMap<>(fieldMap.size());
                    for (String fk : fieldMap.keySet()) {
                        byte[] bk = getStringSerializer().serialize(fk);
                        byte[] bv = getDefRedisSerializer().serialize(fieldMap.get(fk));
                        cacheValue.put(bk, bv);
                    }
                    connection.hMSet(getStringSerializer().serialize(key), cacheValue);
                    return null;
                }
            });
        }
    }

    @Override
    public Object hGet(final String key, final String fieldKey) throws Exception {
        return execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] valueBytes = connection.hGet(getStringSerializer().serialize(key),
                        getStringSerializer().serialize(fieldKey));
                if (valueBytes != null && valueBytes.length > 0) {
                    return getDefRedisSerializer().deserialize(valueBytes);
                }
                return null;
            }
        });
    }

    @Override
    public long hDel(final String key, final String... fieldKeys) throws Exception {
        if (fieldKeys != null && fieldKeys.length > 0) {
            return execute(new RedisCallback<Long>() {
                @Override
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    byte[][] fieldkeyBytes = new byte[fieldKeys.length][];
                    for (int i = 0; i < fieldKeys.length; i++) {
                        fieldkeyBytes[i] = getStringSerializer().serialize(fieldKeys[i]);
                    }
                    return connection.hDel(getStringSerializer().serialize(key), fieldkeyBytes);
                }
            });
        }
        return -1L;
    }

    @Override
    public <T> Map<String, T> hGetAll(final String key, Class<T> clazz) throws Exception {
        return execute(new RedisCallback<Map<String, T>>() {
            @Override
            public Map<String, T> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                Map<byte[], byte[]> byteMap = connection.hGetAll(getStringSerializer().serialize(key));
                if (byteMap != null && !byteMap.isEmpty()) {
                    Map<String, T> valueMap = new HashMap<>();
                    for (byte[] bk : byteMap.keySet()) {
                        String vk = getStringSerializer().deserialize(bk);
                        @SuppressWarnings("unchecked")
                        T vv = (T) getDefRedisSerializer().deserialize(byteMap.get(bk));
                        valueMap.put(vk, vv);
                    }
                    return valueMap;
                }
                return Collections.emptyMap();
            }
        });
    }

    @Override
    public byte[] leftPop(final String key) throws Exception {
        return execute(new RedisCallback<byte[]>() {
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.lPop(keyBytes);
            }
        });
    }

    @Override
    public boolean rightPush(final String key, final byte[] value) throws Exception {
        execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.rPush(keyBytes, value);
            }
        });
        return true;

    }

    @Override
    public boolean expire(final String key, long timeout, TimeUnit unit) throws Exception {
        final long rawTimeout = TimeoutUtils.toSeconds(timeout, unit);
        return execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] bytes = getStringSerializer().serialize(key);
                return connection.expire(bytes, rawTimeout);
            }
        });
    }

    @Override
    public long publish(final String channel, final String msg) throws Exception {
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                long ret = 0;
                try {
                    ret = connection.publish(getStringSerializer().serialize(channel),
                            getStringSerializer().serialize(msg));
                } catch (Exception ignored) {
                }
                return ret;
            }
        });

    }

    @Override
    public long zSize(final String key) throws Exception {
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zCard(keyBytes);
            }
        });
    }

    @Override
    public long zCount(final String key, final double min, final double max) throws Exception {
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                return connection.zCount(getStringSerializer().serialize(key), min, max);
            }
        });
    }

    @Override
    public Set<byte[]> zRangeByScore(final String key, final double min, final double max,
                                     final long offset, final long count) throws Exception {
        return execute(new RedisCallback<Set<byte[]>>() {
            public Set<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRangeByScore(keyBytes, min, max, offset, count);
            }
        });
    }

    @Override
    public long zRemove(final String key, final byte[]... value) throws Exception {
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRem(keyBytes, value);
            }
        }, true);
    }

    @Override
    public boolean zAdd(final String key, final byte[] value, final double score) throws Exception {
        return execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zAdd(keyBytes, score, value);
            }
        }, true);
    }

    @Override
    public byte[] getSet(final String key, final byte[] values, long time, TimeUnit timeUnit)
            throws Exception {
        final long rawTimeout = TimeoutUtils.toSeconds(time, timeUnit);
        return execute(new RedisCallback<byte[]>() {
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                byte[] returnValue = connection.getSet(keyBytes, values);
                if (rawTimeout > 0) {
                    connection.expire(keyBytes, rawTimeout);
                }
                return returnValue;
            }
        });
    }

    @Override
    public Set<byte[]> zRevRangeByScore(final String key, final double min, final double max)
            throws Exception {
        return execute(new RedisCallback<Set<byte[]>>() {
            public Set<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRevRangeByScore(keyBytes, min, max);
            }
        });
    }

    @Override
    public Set<byte[]> zRevRangeByScore(final String key, final double min, final double max,
                                        final long offset, final long count) throws Exception {
        return execute(new RedisCallback<Set<byte[]>>() {
            public Set<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRevRangeByScore(keyBytes, min, max, offset, count);
            }
        });
    }

    @Override
    public Set<byte[]> zRangeByScore(final String key, final double min, final double max)
            throws Exception {
        return execute(new RedisCallback<Set<byte[]>>() {
            public Set<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRangeByScore(keyBytes, min, max);
            }
        });
    }

    @Override
    public void delete(final List<String> keys) throws Exception {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        final byte[][] rawKeys = rawKeys(keys);
        execute(new RedisCallback<Object>() {

            public Object doInRedis(RedisConnection connection) {
                selectDb(connection);
                connection.del(rawKeys);
                return null;
            }
        }, true);

    }

    @Override
    public long zRemoveRangeByScore(final String key, final double min, final double max)
            throws Exception {
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.zRemRangeByScore(keyBytes, min, max);
            }
        });
    }

    @Override
    public long increment(final String key, final Long delta) throws Exception {
        final byte[] rawKey = rawKey(key);
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) {
                selectDb(connection);
                return connection.incrBy(rawKey, delta);
            }
        }, true);
    }

    byte[] rawKey(String key) {
        Assert.notNull(key, "non null key required");
        if (this.redisTemplate.getStringSerializer() == null) {
            return key.getBytes();
        }
        return this.redisTemplate.getStringSerializer().serialize(key);
    }

    private byte[][] rawKeys(Collection<String> keys) {
        final byte[][] rawKeys = new byte[keys.size()][];

        int i = 0;
        for (String key : keys) {
            rawKeys[i++] = getStringSerializer().serialize(key);
        }

        return rawKeys;
    }

    /**
     * 切换DB
     *
     * @param connection
     */
    private void selectDb(RedisConnection connection) {
        if (connection != null) {
            if (connection instanceof BinaryClient) {
                BinaryClient binaryClient = (BinaryClient) connection;
                if (binaryClient.getDB() == dbIndex) {
                    return;
                }
            }
            connection.select(dbIndex);
        }
    }

    /**
     * 执行redis
     *
     * @param action
     * @return T
     * @throws Exception
     */
    private <T> T execute(RedisCallback<T> action) throws Exception {
        try {
            return redisTemplate.execute(action);
        } catch (Exception e) {
            log.error("执行redis发生异常:" + e.getMessage(), e);
            if (e instanceof DataAccessException) {
                DataAccessException de = (DataAccessException) e;
                if (de.getCause() != null) {
                    log.error("执行redis发生DataAccessException.Cause" + de.getCause().getMessage(),
                            de.getCause());
                } else {
                    log.error("执行redis发生DataAccessException:" + de.getMessage(), de);
                }
            }
            throw e;
        }
    }

    /**
     * 执行redis
     *
     * @param action
     * @return T
     */
    private <T> T execute(RedisCallback<T> action, boolean expose) {
        try {
            return redisTemplate.execute(action, expose);
        } catch (Exception e) {
            log.error("执行redis发生异常:" + e.getMessage(), e);
            if (e instanceof DataAccessException) {
                DataAccessException de = (DataAccessException) e;
                if (de.getCause() != null) {
                    log.error("执行redis发生DataAccessException.Cause" + de.getCause().getMessage(),
                            de.getCause());
                } else {
                    log.error("执行redis发生DataAccessException:" + de.getMessage(), de);
                }
            }
            throw e;
        }
    }

    @Override
    public long listSize(final String key) throws Exception {
        return execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.lLen(keyBytes);
            }
        }, true);
    }

    @Override
    public boolean expire(String key, long timeout) throws Exception {
        return this.expire(key, timeout, TimeUnit.SECONDS);
    }

    @Override
    public byte[] getSet(String key, byte[] values, long time) throws Exception {
        return this.getSet(key, values, time, TimeUnit.SECONDS);
    }

    @Override
    public void set(final String key, final String value, final long timeout) throws Exception {
        if (value != null) {
            execute(new RedisCallback<Object>() {
                @Override
                public Object doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    byte[] keybytes = getStringSerializer().serialize(key);
                    connection.setEx(keybytes, timeout, getStringSerializer().serialize(value));
                    return null;
                }

            });
        }
    }

    @Override
    public void set(final String key, final String value) throws Exception {
        if (value != null) {
            execute(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    selectDb(connection);
                    byte[] bytes = getStringSerializer().serialize(key);
                    connection.set(bytes, getStringSerializer().serialize(value));
                    return null;
                }
            });
        }
    }

    @Override
    public String getString(final String key) throws Exception {
        return execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                // if(connection.exists(keyBytes)){
                byte[] valueBytes = connection.get(keyBytes);
                String o = getStringSerializer().deserialize(valueBytes);
                return o;
                // }
                // return null;
            }
        });
    }

    @Override
    public long ttl(final String key) throws Exception {
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                selectDb(connection);
                byte[] keyBytes = getStringSerializer().serialize(key);
                return connection.ttl(keyBytes);
            }
        });
    }

    // singleton
    public RedisCache getRedisCache() {
        if (redisCache == null) {
            redisCache = new RedisCache(this);
        }
        return redisCache;
    }
}
