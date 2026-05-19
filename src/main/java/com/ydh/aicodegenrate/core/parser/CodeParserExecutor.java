package com.ydh.aicodegenrate.core.parser;

import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {
    private final static HtmlCodeParser htmlParser = new HtmlCodeParser();
    private final static MutiFileCodeParser mutiFileParser = new MutiFileCodeParser();

    public static Object executorParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum){
            case HTML -> htmlParser.parseCode(codeContent);
            case MULTI_FILE -> mutiFileParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"无法生成该类型代码"+codeGenTypeEnum);
        };
    }
}
