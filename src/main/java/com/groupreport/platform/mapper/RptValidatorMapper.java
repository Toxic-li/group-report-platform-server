package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptValidator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 校验规则Mapper
 */
@Mapper
public interface RptValidatorMapper extends BaseMapper<RptValidator> {

    @Select("SELECT * FROM rpt_validator WHERE template_id = #{templateId} AND deleted = 0 AND status = 1 ORDER BY priority ASC, id ASC")
    List<RptValidator> selectByTemplateId(@Param("templateId") Long templateId);
}
