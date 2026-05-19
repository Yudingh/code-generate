package com.ydh.aicodegenrate.core.saver;

import com.ydh.aicodegenrate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenrate.ai.model.MultiFileCodeResult;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存执行器
 * 根据代码生成类型执行对应的文件保存
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final MutiFileCodeFileSaverTemplate mutiFileCodeFileSaverTemplate = new MutiFileCodeFileSaverTemplate();

    public static File executeCodeSave(Object result ,CodeGenTypeEnum codeGenTypeEnum){
        return switch (codeGenTypeEnum){
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) result);
            case MULTI_FILE -> mutiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) result);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型错误"+codeGenTypeEnum);
        };
    }
}
