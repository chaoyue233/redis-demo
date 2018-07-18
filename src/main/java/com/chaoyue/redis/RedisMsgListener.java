package com.chaoyue.redis;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;

@Slf4j
public class RedisMsgListener implements MessageListener {
    @Autowired
    private RedisSerializer<String> stringRedisSerializer;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        String topic = stringRedisSerializer.deserialize(message.getChannel());
        String body = stringRedisSerializer.deserialize(message.getBody());
        log.info("order status change topic:" + topic + " body:" + body);
        Map<String, Object> dataMap = JSON.parseObject(body);
        try {

        } catch (Exception e) {
            log.error("error to send message " + body, e);
        }
    }
}
