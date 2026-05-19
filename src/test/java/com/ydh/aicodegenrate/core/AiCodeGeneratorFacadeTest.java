package com.ydh.aicodegenrate.core;

import cn.hutool.core.util.StrUtil;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import javax.swing.*;
import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("帮我生成一个简单的前端登录界面，不超过20行", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("帮我生成一个简单的前端登录界面，不超过20行", CodeGenTypeEnum.HTML);
        List<String> resultBlock = codeStream.collectList().block();
        Assertions.assertNotNull(resultBlock);
        String code = StrUtil.join("", resultBlock);
        Assertions.assertNotNull(code);
    }
}