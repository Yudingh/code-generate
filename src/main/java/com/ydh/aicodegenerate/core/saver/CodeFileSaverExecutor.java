package com.ydh.aicodegenerate.core.saver;

import com.ydh.aicodegenerate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenerate.ai.model.MultiFileCodeResult;
import com.ydh.aicodegenerate.exception.BusinessException;
import com.ydh.aicodegenerate.exception.ErrorCode;
import com.ydh.aicodegenerate.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存执行器
 * 根据代码生成类型执行对应的文件保存
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final MutiFileCodeFileSaverTemplate mutiFileCodeFileSaverTemplate = new MutiFileCodeFileSaverTemplate();

    public static File executeCodeSave(Object result ,CodeGenTypeEnum codeGenTypeEnum, Long appId){
        return switch (codeGenTypeEnum){
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) result, appId);
            case MULTI_FILE -> mutiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) result,appId);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型错误"+codeGenTypeEnum);
        };
    }
}
