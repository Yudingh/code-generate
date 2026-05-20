package com.ydh.aicodegenrate.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ydh.aicodegenrate.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ydh.aicodegenrate.model.entity.ChatHistory;
import com.ydh.aicodegenrate.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author Nithti
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 新增历史对话
     * @param appId 应用id
     * @param message 消息
     * @param messageType 消息类型(AI/USER)
     * @param userId 用户id
     * @return 是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType,Long userId);

    /**
     * 清理历史对话
     * @param appId 应用id
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取游标分页查询的查询包装类
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 基于游标的分页查询
     * @param appId appId
     * @param pageSize 页面大小
     * @param lastCreateTime 上一条时间
     * @param lastId 上一条id
     * @param user 登录用户
     * @return 分页对话
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, Long lastId, User user);
}
