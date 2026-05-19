package com.ydh.aicodegenrate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ydh.aicodegenrate.ai.model.MultiFileCodeResult;
import com.ydh.aicodegenrate.exception.BusinessException;
import com.ydh.aicodegenrate.exception.ErrorCode;
import com.ydh.aicodegenrate.model.enums.CodeGenTypeEnum;

public class MutiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 保存HTML文件
        WriteToFile(baseDirPath,"index.html", result.getHtmlCode());
        // 保存CSS文件
        WriteToFile(baseDirPath,"style.css", result.getCssCode());
        // 保存JS文件
        WriteToFile(baseDirPath,"script.js", result.getJsCode());
    }
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码为空");
        }
    }
}
