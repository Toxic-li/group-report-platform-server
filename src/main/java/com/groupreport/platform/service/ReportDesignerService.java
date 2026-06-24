package com.groupreport.platform.service;

import com.groupreport.platform.dto.DesignerTemplateDTO;
import com.groupreport.platform.dto.TemplateQueryDTO;
import com.groupreport.platform.common.PageResult;
import com.groupreport.platform.vo.ReportDesignerTemplateVO;
import com.groupreport.platform.vo.TemplateListItemVO;

import java.util.List;
import java.util.Map;

/**
 * 报表设计器核心服务接口
 * 负责完整模板JSON的组装与拆分（设计器 ↔ 数据库）
 */
public interface ReportDesignerService {

    /**
     * 加载完整模板JSON（从各表组装）
     * @param templateId 模板ID
     * @return 完整模板定义
     */
    ReportDesignerTemplateVO loadFullTemplate(Long templateId);

    Long saveTemplate(DesignerTemplateDTO dto);

    /**
     * 保存完整模板JSON（拆分到各表）
     * @param templateVO 完整模板定义
     * @return 模板ID
     */
    Long saveFullTemplate(ReportDesignerTemplateVO templateVO);

    /**
     * 更新完整模板JSON
     */
    void updateFullTemplate(Long templateId, ReportDesignerTemplateVO templateVO);

    /**
     * 发布设计（含校验、版本快照）
     */
    void publishDesign(Long templateId);

    /**
     * 复制模板
     */
    Long copyTemplate(Long sourceTemplateId, String newName);

    /**
     * 从JSON导入模板
     */
    Long importFromJson(ReportDesignerTemplateVO templateVO);

    /**
     * 获取可用数据源列表
     */
    List<?> listAvailableDataSources();

    /**
     * 预览模板效果
     */
    ReportDesignerTemplateVO.PreviewResult previewTemplate(Long templateId, Long orgId, String period);

    /**
     * 分页查询模板列表（仅基本信息，不含完整templateJson）
     */
    PageResult<TemplateListItemVO> listTemplates(TemplateQueryDTO query);
}
