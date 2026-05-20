package com.ydh.aicodegenrate.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ydh.aicodegenrate.constant.UserConstant;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.exception.ThrowUtils;
import com.ydh.aicodegenrate.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ydh.aicodegenrate.model.entity.App;
import com.ydh.aicodegenrate.model.entity.ChatHistory;
import com.ydh.aicodegenrate.mapper.ChatHistoryMapper;
import com.ydh.aicodegenrate.model.entity.User;
import com.ydh.aicodegenrate.model.enums.ChatHistoryMessageTypeEnum;
import com.ydh.aicodegenrate.service.AppService;
import com.ydh.aicodegenrate.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author Nithti
 */
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
}
