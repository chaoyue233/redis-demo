package com.chaoyue.redis;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;

/**
 * redis 订阅监听
 * 需要为每一个 channel 实现一个 MessageListener 并在 spring中配置 listener-container
 */
@Slf4j
public class RedisMsgListener implements MessageListener {
    @Autowired
    private RedisSerializer<String> stringRedisSerializer;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = stringRedisSerializer.deserialize(message.getChannel());
        String body = stringRedisSerializer.deserialize(message.getBody());
        log.info("order status change topic:" + topic + " body:" + body);
        try {
            // 一般消息发布以json格式 当然也可以使用其他方式
            Map<String, Object> dataMap = JSON.parseObject(body);
        } catch (Exception e) {
            log.error("error to send message " + body, e);
        }
    }
}
