package com.ydh.aicodegenerate.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.ydh.aicodegenerate.annotation.AuthCheck;
import com.ydh.aicodegenerate.common.BaseResponse;
import com.ydh.aicodegenerate.common.ResultUtils;
import com.ydh.aicodegenerate.constant.UserConstant;
import com.ydh.aicodegenerate.exception.ErrorCode;
import com.ydh.aicodegenerate.exception.ThrowUtils;
import com.ydh.aicodegenerate.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.ydh.aicodegenerate.model.entity.ChatHistory;
import com.ydh.aicodegenerate.model.entity.User;
import com.ydh.aicodegenerate.service.ChatHistoryService;
import com.ydh.aicodegenerate.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史 控制层。
 *
 * @author Nithti
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 游标分页查询应用历史对话
     * @param chatHistoryQueryRequest 查询请求
     * @param request http请求
     * @return 历史对话分页数据
     */
    @PostMapping("/app/list/page")
    public BaseResponse<Page<ChatHistory>> listAppChatHistoryByPage(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(chatHistoryQueryRequest.getAppId() == null, ErrorCode.PARAMS_ERROR, "应用ID错误");
        int pageSize = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listAppChatHistoryByPage(
                chatHistoryQueryRequest.getAppId(),
                pageSize,
                chatHistoryQueryRequest.getLastCreateTime(),
                chatHistoryQueryRequest.getLastId(),
                loginUser
        );
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员分页查询历史对话（可查看所有用户）
     * @param chatHistoryQueryRequest 查询请求
     * @return 历史对话分页数据
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAdminChatHistoryByPage(
            @RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageSize = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        boolean hasCursor = chatHistoryQueryRequest.getLastCreateTime() != null || chatHistoryQueryRequest.getLastId() != null;
        ThrowUtils.throwIf(
                (chatHistoryQueryRequest.getLastCreateTime() == null) != (chatHistoryQueryRequest.getLastId() == null),
                ErrorCode.PARAMS_ERROR,
                "游标参数不完整"
        );
        int pageNum = hasCursor ? 1 : chatHistoryQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageNum <= 0, ErrorCode.PARAMS_ERROR, "页号错误");
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }

}
