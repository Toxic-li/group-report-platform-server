package com.groupreport.platform.service;

import com.groupreport.platform.dto.CellDataDTO;
import com.groupreport.platform.dto.ReportDataSaveDTO;
import com.groupreport.platform.vo.ReportDataVO;
import com.groupreport.platform.vo.TemplateDetailVO;

import java.util.List;
import java.util.Map;

/**
 * 报表数据服务接口
 */
public interface ReportDataService {

    /**
     * 获取报表数据（用于前端Univer回写）
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @return 报表数据（含模板结构和数据内容）
     */
    ReportDataVO getReportData(Long templateId, Long orgId, String period);

    /**
     * 保存报表数据（草稿/自动保存）
     * @param saveDTO 数据保存请求
     * @return 保存结果
     */
    Map<String, Object> saveData(ReportDataSaveDTO saveDTO);

    /**
     * 批量保存单元格数据
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @param cells 单元格数据列表
     * @return 保存的记录数
     */
    int batchSaveCells(Long templateId, Long orgId, String period, List<CellDataDTO> cells);

    /**
     * 获取指定单元格的值
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @param rowCode 行编码
     * @param columnCode 列编码
     * @return 单元格值
     */
    Object getCellValue(Long templateId, Long orgId, String period, String rowCode, String columnCode);

    /**
     * 清空报表数据
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     */
    void clearData(Long templateId, Long orgId, String period);

    /**
     * 数据校验
     * @param templateId 模板ID
     * @param orgId 组织ID
     * @param period 周期
     * @return 校验结果（错误列表为空则校验通过）
     */
    List<String> validateData(Long templateId, Long orgId, String period);
}
