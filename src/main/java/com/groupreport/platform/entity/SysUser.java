package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码（加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 性别：0-未知 1-男 2-女 */
    private Integer gender;

    /** 所属组织ID */
    private Long orgId;

    /** 职位 */
    private String position;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

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
