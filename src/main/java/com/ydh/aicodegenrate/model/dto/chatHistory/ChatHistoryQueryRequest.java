package com.ydh.aicodegenrate.model.dto.chatHistory;

import com.ydh.aicodegenrate.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史游标查询请求
 *
 * @author Nithti
 */
@EqualsAndHashCode(callSuper=false)
@Data
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录
     */
    private LocalDateTime lastCreateTime;

    /**
     * 游标查询 - 最后一条记录的 id
     * 与 lastCreateTime 组成联合游标，避免同一时间戳下重复/漏查
     */
    private Long lastId;

    private static final long serialVersionUID = 1L;
}
