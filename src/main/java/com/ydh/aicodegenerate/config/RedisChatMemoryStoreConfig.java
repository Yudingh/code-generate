package com.ydh.aicodegenerate.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

import java.lang.reflect.Field;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

    private String host;

    private int port;

    private String password;

    private long ttl;

    private int database;
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() throws Exception {
        RedisChatMemoryStore store = RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .user("default")
                .password(password)
                .ttl(ttl)
                .build();
        // 手动使用JedisPool实例化支持不同数据库选择的实例
        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .user("default")
                .password(password)
                .database(database) // 🌟 在这里明明白白塞入你的 1 号库！
                .build();
        HostAndPort address = new HostAndPort(host, port);
        JedisPooled realClient = new JedisPooled(address, clientConfig);
        // 通过反射将realClient替换掉store
        Field clientField = RedisChatMemoryStore.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(store,realClient);

        return store;
    }
}
