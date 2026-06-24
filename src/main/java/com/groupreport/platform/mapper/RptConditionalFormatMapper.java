package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptConditionalFormat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 条件格式Mapper
 */
@Mapper
public interface RptConditionalFormatMapper extends BaseMapper<RptConditionalFormat> {

    @Select("SELECT * FROM rpt_conditional_format WHERE template_id = #{templateId} AND deleted = 0 AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<RptConditionalFormat> selectByTemplateId(@Param("templateId") Long templateId);
}
