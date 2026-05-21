package com.ydh.aicodegenerate.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
@Description("生成HTML代码文件结果")
@Data
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("对生成代码的描述")
    private String description;
}
