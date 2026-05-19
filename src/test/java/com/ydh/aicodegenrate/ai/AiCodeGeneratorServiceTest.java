package com.ydh.aicodegenrate.ai;

import com.ydh.aicodegenrate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenrate.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHTMLCode() {
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHTMLCode("帮我生成一个简单的个人博客界面，代码不超过20行");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("帮我生成一个简单的个人博客界面，代码不超过20行");
        Assertions.assertNotNull(multiFileCode);
    }
}