package com.ydh.aicodegenrate.ai;

import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {
    /**
     * 生成单HTML文件的代码
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-single-html.txt")
    String generateHTMLCode(String userMessage);

    /**
     * 生成多文件的代码
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-multi-file.txt")
    String generateMultiFileCode(String userMessage);
}
