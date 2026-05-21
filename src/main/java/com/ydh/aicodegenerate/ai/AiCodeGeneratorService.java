package com.ydh.aicodegenerate.ai;

import com.ydh.aicodegenerate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenerate.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 生成单HTML文件的代码
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-single-html.txt")
    HtmlCodeResult generateHTMLCode(String userMessage);

    /**
     * 生成多文件的代码
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-multi-file.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成单HTML文件的代码(流式输出)
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-single-html.txt")
    Flux<String> generateHTMLCodeStream(String userMessage);

    /**
     * 生成多文件的代码(流式输出)
     * @param userMessage 用户提示词
     * @return 生成结果
     */
    @SystemMessage(fromResource = "prompt/code-generate-prompt-multi-file.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
