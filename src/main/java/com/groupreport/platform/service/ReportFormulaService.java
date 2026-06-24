package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.FormulaDTO;
import com.groupreport.platform.entity.RptFormula;
import com.groupreport.platform.vo.FormulaVO;

import java.util.List;
import java.util.Map;

/**
 * 公式服务接口
 */
public interface ReportFormulaService extends IService<RptFormula> {

    /**
     * 根据模板ID获取公式列表
     * @param templateId 模板ID
     * @return 公式列表
     */
    List<FormulaVO> getFormulasByTemplateId(Long templateId);

    /**
     * 获取公式详情
     * @param id 公式ID
     * @return 公式信息
     */
    FormulaVO getFormulaDetail(Long id);

    /**
     * 创建公式
     * @param dto 公式信息
     * @return 公式ID
     */
    Long createFormula(FormulaDTO dto);

    /**
     * 更新公式
     * @param dto 公式信息
     */
    void updateFormula(FormulaDTO dto);

    /**
     * 删除公式
     * @param id 公式ID
     */
    void deleteFormula(Long id);

    /**
     * 启用/禁用公式
     * @param id 公式ID
     * @param status 状态 0-禁用 1-启用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 执行指定模板的所有公式计算（按优先级排序）
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @param calcTrigger 触发类型：1-实时 2-保存时 3-提交时
     * @return 计算结果 Map<rowCode:columnCode, value>
     */
    Map<String, String> executeFormulas(Long templateId, Long orgId, String period, Integer calcTrigger);

    /**
     * 执行单个公式计算
     * @param formulaId 公式ID
     * @param orgId 组织ID
     * @param period 周期
     * @return 计算结果值
     */
    String executeSingleFormula(Long formulaId, Long orgId, String period);
}
