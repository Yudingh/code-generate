package com.ydh.aicodegenrate.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 模板设计模式
 * 抽象文件保存类
 */
public abstract class CodeFileSaverTemplate<T> {
    // 定义文件保存根目录
    protected final static String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法：定义文件保存的流程
     * @param result 代码结果对象
     * @return 保存目录
     */
    public final File saveCode(T result){
        // 验证输入
        validateInput(result);
        // 构建唯一目录
        String uniqueDir = buildUniqueDir();
        // 写入文件
        saveFiles(result,uniqueDir);
        // 返回文件目录
        return new File(uniqueDir);
    }

    protected void validateInput(T result){
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"保存对象为空");
        }
    }

    /**
     * 构建唯一目录路径
     * @return 目录
     */
    protected final String buildUniqueDir(){
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
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
    protected final void WriteToFile(String dirPath, String fileName, String content){
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content,filePath, StandardCharsets.UTF_8);
    }

    // 抽象类，交由具体子类实现
    protected abstract CodeGenTypeEnum getCodeType();

    // 抽象类，交由具体子类实现
    protected abstract void saveFiles(T result, String baseDirPath);
}
