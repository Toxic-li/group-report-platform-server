package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptFormula;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 公式Mapper
 */
@Mapper
public interface RptFormulaMapper extends BaseMapper<RptFormula> {

    /**
     * 根据模板ID查询公式列表（按优先级排序）
     */
    @Select("SELECT * FROM rpt_formula WHERE template_id = #{templateId} AND deleted = 0 AND status = 1 ORDER BY priority ASC, id ASC")
    List<RptFormula> selectByTemplateId(@Param("templateId") Long templateId);

    /**
     * 根据目标单元格查询公式
     */
    @Select("SELECT * FROM rpt_formula WHERE template_id = #{templateId} AND target_row_code = #{rowCode} AND target_column_code = #{columnCode} AND deleted = 0 AND status = 1")
    List<RptFormula> selectByTargetCell(@Param("templateId") Long templateId, @Param("rowCode") String rowCode, @Param("columnCode") String columnCode);

    /**
     * 根据计算触发类型查询公式
     */
    @Select("SELECT * FROM rpt_formula WHERE template_id = #{templateId} AND calc_trigger = #{calcTrigger} AND deleted = 0 AND status = 1 ORDER BY priority ASC")
    List<RptFormula> selectByCalcTrigger(@Param("templateId") Long templateId, @Param("calcTrigger") Integer calcTrigger);
}
