package com.ydh.aicodegenrate.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ydh.aicodegenrate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenrate.ai.model.MultiFileCodeResult;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Deprecated
public class CodeFileSaver {
    // 文件保存根目录
    private final static String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 构建唯一目录路径
     * @param bizType 类型
     * @return 目录
     */
    private static String buildUniqueDir(String bizType){
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     * @param dirPath 目录路径
     * @param fileName 文件名
     * @param content 内容
     */
    private static void WriteToFile(String dirPath, String fileName, String content){
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }

    /**
     * 写入HTML文件
     * @param htmlCodeResult html格式输出
     * @return html文件
     */
    public static File saveHtmlCode(HtmlCodeResult htmlCodeResult){
        String dirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        WriteToFile(dirPath,"index.html",htmlCodeResult.getHtmlCode());
        return new File(dirPath);
    }

    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult){
        String dirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        // html
        WriteToFile(dirPath,"index.html",multiFileCodeResult.getHtmlCode());
        // css
        WriteToFile(dirPath,"style.css",multiFileCodeResult.getCssCode());
        // js
        WriteToFile(dirPath,"script.js",multiFileCodeResult.getJsCode());

        return new File(dirPath);
    }
}
