package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptTemplateRow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模板行结构Mapper
 */
@Mapper
public interface RptTemplateRowMapper extends BaseMapper<RptTemplateRow> {

    /**
     * 物理删除指定模板的所有行（包括逻辑删除的记录）
     */
    @Delete("DELETE FROM rpt_template_row WHERE template_id = #{templateId}")
    void physicalDeleteByTemplateId(@Param("templateId") Long templateId);
}
