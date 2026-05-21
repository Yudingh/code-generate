package com.ydh.aicodegenerate.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ydh.aicodegenerate.constant.UserConstant;
import com.ydh.aicodegenerate.exception.BusinessException;
import com.ydh.aicodegenerate.exception.ErrorCode;
import com.ydh.aicodegenerate.exception.ThrowUtils;
import com.ydh.aicodegenerate.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ydh.aicodegenerate.model.entity.App;
import com.ydh.aicodegenerate.model.entity.ChatHistory;
import com.ydh.aicodegenerate.mapper.ChatHistoryMapper;
import com.ydh.aicodegenerate.model.entity.User;
import com.ydh.aicodegenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.ydh.aicodegenerate.service.AppService;
import com.ydh.aicodegenerate.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author Nithti
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID错误");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容为空");
        ThrowUtils.throwIf(ChatHistoryMessageTypeEnum.getEnumByValue(messageType) == null,
                ErrorCode.PARAMS_ERROR, "消息类型错误");
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId==null ||appId < 0, ErrorCode.PARAMS_ERROR,"应用ID错误");
        QueryWrapper queryWrapper = QueryWrapper.create().eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        Long lastId = chatHistoryQueryRequest.getLastId();
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 联合游标：createTime 倒序 + id 倒序
        if (lastCreateTime != null && lastId != null) {
            queryWrapper.and("(createTime < ? OR (createTime = ? AND id < ?))",
                    lastCreateTime, lastCreateTime, lastId);
        }
        queryWrapper.orderBy("createTime", false)
                .orderBy("id", false);
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, Long lastId, User user) {
        ThrowUtils.throwIf(appId == null || appId < 0,ErrorCode.PARAMS_ERROR,"应用ID错误");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR,"页面大小必须在0-50之间");
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        ThrowUtils.throwIf((lastCreateTime == null) != (lastId == null), ErrorCode.PARAMS_ERROR, "游标参数不完整");
        // 只有自己和管理员才可以查看历史对话
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 判断是不是管理员
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(user.getUserRole());
        // 判断是不是当前用户
        boolean isCreator = app.getUserId().equals(user.getId());
        if (!isCreator && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无权查看历史对话");
        }
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        queryRequest.setLastId(lastId);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询条件
        return this.page(Page.of(1,pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 1. 从数据库中查询历史对话
            // 从 1 开始查询历史消息，是因为在对话流程中用户消息首先被加入到数据库，AI服务也会自动将用户消息添加到记忆中，如果不排除会导致重复加载
            QueryWrapper wrapper = QueryWrapper.create().eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(wrapper);
            // 2. 判断是否为空
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 3. 反转列表，确保先创建的对话在前
            historyList = historyList.reversed();
            // 4. 将对话加入记忆
            int count = 0;
            // 防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    count++;
                }else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    count++;
                }
            }
            log.info("成功为应用：{}，加载{}条消息：",appId,count);
            return count;
        }catch (Exception e) {
            log.info("应用：{}，加载历史消息失败:{}：",appId,e.getMessage());
            return 0;
        }
    }
}
