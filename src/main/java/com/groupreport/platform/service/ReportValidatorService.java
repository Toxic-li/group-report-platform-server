package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.ValidatorDTO;
import com.groupreport.platform.entity.RptValidator;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.ValidatorDef;
import com.groupreport.platform.vo.ValidatorVO;

import java.util.List;
import java.util.Map;

/**
 * 校验规则服务接口
 */
public interface ReportValidatorService extends IService<RptValidator> {

    List<ValidatorVO> getValidatorsByTemplateId(Long templateId);

    ValidatorVO getValidatorDetail(Long id);

    Long createValidator(ValidatorDTO dto);

    void updateValidator(ValidatorDTO dto);

    void deleteValidator(Long id);

    /**
     * 执行校验（核心方法）
     * @param templateId 模板ID
     * @param cellData 待校验的单元格数据 Map<rowCode:columnCode, value>
     * @param trigger 校验时机：1-输入时 2-保存时 3-提交时
     * @return 校验结果：key=错误位置(rowCode:columnCode), value=错误信息
     */
    Map<String, String> validateData(Long templateId, Map<String, String> cellData, Integer trigger);

    /**
     * 批量从设计器JSON导入校验规则
     */
    void importFromDesignerJson(Long templateId, List<ValidatorDef> validators);

    /**
     * 导出为设计器JSON格式
     */
    List<ValidatorDef> exportToDesignerJson(Long templateId);
}
