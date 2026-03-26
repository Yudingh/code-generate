package com.ydh.aicodegenrate.ai;

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
        String htmlCode = aiCodeGeneratorService.generateHTMLCode("帮我生成个人博客界面，博客名为：nithti,不超过20行");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        String multiFileCode = aiCodeGeneratorService.generateMultiFileCode("帮我生成个人博客界面，博客名为：nithti，不超过20行");
        Assertions.assertNotNull(multiFileCode);
    }
}