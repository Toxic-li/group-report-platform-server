package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.ConditionalFormatDTO;
import com.groupreport.platform.entity.RptConditionalFormat;
import com.groupreport.platform.vo.ConditionalFormatVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.ConditionalFormatDef;

import java.util.List;

/**
 * 条件格式服务接口
 */
public interface ReportConditionalFormatService extends IService<RptConditionalFormat> {

    List<ConditionalFormatVO> getFormatsByTemplateId(Long templateId);

    ConditionalFormatVO getFormatDetail(Long id);

    Long createFormat(ConditionalFormatDTO dto);

    void updateFormat(ConditionalFormatDTO dto);

    void deleteFormat(Long id);

    /**
     * 根据模板+数据计算条件格式结果
     * 返回每个需要应用样式的单元格及其样式配置
     */
    List<ConditionalFormatResult> evaluateFormats(Long templateId, java.util.Map<String, String> cellData);

    record ConditionalFormatResult(String cellKey, String styleConfig) {}

    /**
     * 从设计器JSON导入条件格式规则
     */
    void importFromDesignerJson(Long templateId, List<ConditionalFormatDef> formats);

    /**
     * 导出为设计器JSON格式
     */
    List<ConditionalFormatDef> exportToDesignerJson(Long templateId);
}
