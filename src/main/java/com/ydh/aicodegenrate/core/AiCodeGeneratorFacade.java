package com.ydh.aicodegenrate.core;

import com.ydh.aicodegenrate.ai.AiCodeGeneratorService;
import com.ydh.aicodegenrate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenrate.ai.model.MultiFileCodeResult;
import com.ydh.aicodegenrate.core.parser.CodeParserExecutor;
import com.ydh.aicodegenrate.core.saver.CodeFileSaverExecutor;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.exception.ThrowUtils;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成的外观类，组合生成和保存能力
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    /**
     * 统一入口，根据用户提示词和生成类型，生成并保存代码
     * @param userMessage 用户提示词
     * @param codeGenType 生成类型
     * @return 保存的文件
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }
        if (userMessage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户输入不能为空");
        }
        switch (codeGenType) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode(userMessage);
                return CodeFileSaverExecutor.executeCodeSave(htmlCodeResult,codeGenType,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                return CodeFileSaverExecutor.executeCodeSave(multiFileCodeResult,codeGenType,appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"无效生成类型");
        }
    }

    /**
     * 统一入口，根据用户提示词和生成类型，生成并保存代码（流式调用）
     * @param userMessage 用户输入
     * @param codeGenType 生成类型
     * @return 流式消息
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType,Long appId) {
        if (codeGenType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }
        if (userMessage == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户输入不能为空");
        }
        switch (codeGenType) {
            case HTML -> {
                Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHTMLCodeStream(userMessage);
                return processCodeStream(htmlCodeStream,codeGenType,appId);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                return processCodeStream(multiFileCodeStream,codeGenType,appId);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"无效生成类型");
        }
    }

    /**
     * 流式通过处理
     * @param codeStream AI流式返回结果
     * @param codeGenType 生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, Long appId) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(codeBuilder::append)
                .doOnComplete(()->{
                    try {
                        String completeCode = codeBuilder.toString();
                        // 调用代码解析执行器
                        Object parser = CodeParserExecutor.executorParser(completeCode, codeGenType);
                        // 调用文件保存执行器
                        File file = CodeFileSaverExecutor.executeCodeSave(parser, codeGenType,appId);
                        log.info("文件保存成功：{}", file.getAbsolutePath());
                    }catch (Exception e){
                        log.error("文件保存失败：{}", e.getMessage());
                    }
                });
    }

}
