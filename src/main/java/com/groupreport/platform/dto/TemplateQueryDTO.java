package com.groupreport.platform.dto;

import lombok.Data;

/**
 * 模板列表查询条件
 */
@Data
public class TemplateQueryDTO {

    /** 模板名称（模糊搜索） */
    private String name;

    /** 模板类型：1-统计报表 2-填报报表 3-汇总报表 */
    private Integer templateType;

    /** 状态：0-草稿 1-已发布 2-已停用 */
    private Integer status;

    /** 当前页码 */
    private Integer current = 1;

    /** 每页条数 */
    private Integer size = 10;
}
