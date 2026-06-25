package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptTemplateColumn;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模板列结构Mapper
 */
@Mapper
public interface RptTemplateColumnMapper extends BaseMapper<RptTemplateColumn> {

    /**
     * 物理删除指定模板的所有列（包括逻辑删除的记录）
     */
    @Delete("DELETE FROM rpt_template_column WHERE template_id = #{templateId}")
    void physicalDeleteByTemplateId(@Param("templateId") Long templateId);
}
