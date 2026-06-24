package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.DesignerTemplateDTO;
import com.groupreport.platform.dto.TemplateDTO;
import com.groupreport.platform.entity.RptTemplate;
import com.groupreport.platform.vo.ReportDesignerTemplateVO;
import com.groupreport.platform.vo.TemplateDetailVO;

import java.util.List;

/**
 * 报表模板服务接口
 */
public interface ReportTemplateService extends IService<RptTemplate> {

    /**
     * 获取模板完整结构（包含行列配置）
     * @param templateId 模板ID
     * @return 模板完整信息（用于前端Univer渲染）
     */
    DesignerTemplateDTO getTemplateDetail(Long templateId);

    /**
     * 获取所有已发布的模板列表
     * @return 模板列表
     */
    List<DesignerTemplateDTO> getPublishedTemplates();

    /**
     * 创建模板（含行列配置）
     * @param templateDTO 模板信息
     * @return 模板ID
     */
    Long createTemplate(TemplateDTO templateDTO);

    /**
     * 更新模板（含行列配置）
     * @param templateDTO 模板信息
     */
    void updateTemplate(TemplateDTO templateDTO);

    /**
     * 发布模板
     * @param templateId 模板ID
     * @return 发布后的完整模板信息
     */
    ReportDesignerTemplateVO publishTemplate(Long templateId);

    /**
     * 停用模板
     * @param templateId 模板ID
     */
    void disableTemplate(Long templateId);

    /**
     * 删除模板
     * @param templateId 模板ID
     */
    void deleteTemplate(Long templateId);
}
