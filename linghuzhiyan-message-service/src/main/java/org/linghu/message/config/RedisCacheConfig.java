package org.linghu.message.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息服务Redis缓存配置类
 * 
 * @author linghu
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.cache.redis.time-to-live:PT30M}")
    private Duration timeToLive;

    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> serializer = createJsonSerializer();

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建Jackson2JsonRedisSerializer
        Jackson2JsonRedisSerializer<Object> serializer = createJsonSerializer();

        // 配置序列化对
        RedisSerializationContext.SerializationPair<Object> pair = 
            RedisSerializationContext.SerializationPair.fromSerializer(serializer);

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(timeToLive) // 设置缓存的默认过期时间
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(pair) // 设置value的序列化
            .disableCachingNullValues(); // 不缓存空值

        // 设置不同缓存的过期时间
        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        
        // 消息模板缓存 - 1小时
        configMap.put("messageTemplates", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
        
        // 消息内容缓存 - 15分钟（消息内容更新频繁）
        configMap.put("messages", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 用户消息统计缓存 - 5分钟
        configMap.put("messageStats", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 未读消息计数缓存 - 1分钟（需要实时更新）
        configMap.put("unreadCounts", defaultCacheConfig.entryTtl(Duration.ofMinutes(1)));
        
        // 邮件配置缓存 - 6小时
        configMap.put("emailConfig", defaultCacheConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(configMap)
            .build();
    }

    /**
     * 创建JSON序列化器
     */
    private Jackson2JsonRedisSerializer<Object> createJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        
        return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    }
}
