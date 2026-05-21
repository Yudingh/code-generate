package com.ydh.aicodegenerate.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ydh.aicodegenerate.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {
    @Resource
    private ChatModel chatModel;
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource
    private StreamingChatModel streamingChatModel;
    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI服务缓存实例
     * 缓存策略：最大缓存1000个实例，写入后30分钟过期，访问后10分钟过期
     */
    private final Cache<Long,AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener(((key, value, cause) -> {
                log.debug("AI应用实例被删除，appId：{}，原因：{}", key,cause);
            }))
            .build();



    /**
     * 根据AppId从缓存中获取访问
     * @param appId appId
     * @return AI应用服务
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId,this::createAiCodeGeneratorService);
    }

    /**
     * 根据AppId生成服务实例
     * @param appId appId
     * @return AI应用服务
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        log.info("为appId：{} 创建新的AI应用服务实例",appId);
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 将持久化的历史对话加载到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId,memory,20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(memory).build();
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

}
