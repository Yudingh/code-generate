package com.ydh.aicodegenrate.core.parser;

public interface CodeParser<T> {
    /**
     * 代码解析器（策略接口）
     * @param codeContent 用户输入
     * @return 解析后类型（HTML，Multi-File）
     */
    T parseCode(String codeContent);
}
