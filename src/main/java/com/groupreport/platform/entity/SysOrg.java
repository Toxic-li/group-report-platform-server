package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织机构实体
 */
@Data
@TableName("sys_org")
public class SysOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父级ID，0为顶级 */
    private Long parentId;

    /** 组织编码 */
    private String orgCode;

    /** 组织名称 */
    private String orgName;

    /** 组织类型：1-集团 2-子公司 3-部门 4-小组 */
    private Integer orgType;

    /** 排序号 */
    private Integer sortOrder;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 地址 */
    private String address;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 树路径 */
    private String treePath;

    /** 层级 */
    private Integer level;

    /** 备注 */
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
