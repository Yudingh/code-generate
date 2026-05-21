package com.ydh.aicodegenerate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.ydh.aicodegenerate.ai.model.HtmlCodeResult;
import com.ydh.aicodegenerate.exception.BusinessException;
import com.ydh.aicodegenerate.exception.ErrorCode;
import com.ydh.aicodegenerate.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存模板类
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        WriteToFile(baseDirPath,"index.html",result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result){
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码内容为空");
        }
    }
}
