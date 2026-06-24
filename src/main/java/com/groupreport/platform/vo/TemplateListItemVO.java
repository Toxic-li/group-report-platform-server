package com.groupreport.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板列表项VO（不含完整templateJson，仅基本信息）
 */
@Data
public class TemplateListItemVO {

    private Long id;
    private String templateCode;
    private String templateName;
    private Integer templateType;
    private Integer status;
    private Integer version;
    private Integer periodType;
    private Integer auditRequired;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
