package com.imokkkk.push.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.imokkkk.push.constant.Constants;
import com.imokkkk.push.service.MessageReceiver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author liuwy
 * @date 2023-05-06 15:26
 * @since 1.0
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        // key的序列化采用StringRedisSerializer
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 使用FastJsonRedisSerializer 替换默认序列化(默认采用的是JDK序列化)
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer =
                new FastJsonRedisSerializer<>(Object.class);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer =
                new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);

        // 订阅频道(推送消息)
        redisMessageListenerContainer.addMessageListener(
                listenerAdapter, new PatternTopic(Constants.PUSH_MESSAGE));
        return redisMessageListenerContainer;
    }

    /** 表示监听一个频道（推送消息） */
    @Bean
    public MessageListenerAdapter pushListenerAdapter(MessageReceiver messageReceiver) {
        // 这个地方 是给messageListenerAdapter 传入一个消息接受的处理器，利用反射的方法调用“receive ”
        return new MessageListenerAdapter(messageReceiver, "receive");
    }
}
