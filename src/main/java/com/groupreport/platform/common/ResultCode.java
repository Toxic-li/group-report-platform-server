package com.groupreport.platform.common;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 操作失败 */
    ERROR(500, "操作失败"),

    /** 参数错误 */
    PARAM_ERROR(400, "参数错误"),

    /** 未授权 */
    UNAUTHORIZED(401, "未授权，请登录"),

    /** 禁止访问 */
    FORBIDDEN(403, "禁止访问，权限不足"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 方法不允许 */
    METHOD_NOT_ALLOWED(405, "方法不允许"),

    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /** 用户名或密码错误 */
    LOGIN_ERROR(1001, "用户名或密码错误"),

    /** 账号已被禁用 */
    ACCOUNT_DISABLED(1002, "账号已被禁用"),

    /** Token已过期或无效 */
    TOKEN_INVALID(1003, "Token无效或已过期"),

    /** Token已过期 */
    TOKEN_EXPIRED(1004, "Token已过期，请重新登录"),

    /** 账号不存在 */
    USER_NOT_FOUND(1005, "用户不存在"),

    /** 旧密码不正确 */
    OLD_PASSWORD_ERROR(1006, "旧密码不正确"),

    /** 组织机构不存在 */
    ORG_NOT_FOUND(2001, "组织机构不存在"),

    /** 组织机构编码已存在 */
    ORG_CODE_EXISTS(2002, "组织机构编码已存在"),

    /** 存在子节点，不能删除 */
    ORG_HAS_CHILDREN(2003, "存在子节点，不能删除"),

    /** 角色不存在 */
    ROLE_NOT_FOUND(3001, "角色不存在"),

    /** 角色编码已存在 */
    ROLE_CODE_EXISTS(3002, "角色编码已存在"),

    /** 报表模板不存在 */
    TEMPLATE_NOT_FOUND(4001, "报表模板不存在"),

    /** 报表模板编码已存在 */
    TEMPLATE_CODE_EXISTS(4002, "报表模板编码已存在"),

    /** 报表数据不存在 */
    DATA_NOT_FOUND(4003, "报表数据不存在"),

    /** 提交记录不存在 */
    SUBMIT_NOT_FOUND(4004, "提交记录不存在"),

    /** 公式不存在 */
    FORMULA_NOT_FOUND(4007, "公式不存在"),

    /** 校验规则不存在 */
    VALIDATOR_NOT_FOUND(4008, "校验规则不存在"),

    /** 条件格式规则不存在 */
    CONDITIONAL_FORMAT_NOT_FOUND(4009, "条件格式规则不存在"),

    /** 数据源不存在 */
    DATA_SOURCE_NOT_FOUND(4010, "数据源不存在"),

    /** 当前状态不允许此操作 */
    STATUS_NOT_ALLOWED(4005, "当前状态不允许此操作"),

    /** 数据校验失败 */
    VALIDATE_FAILED(4006, "数据校验失败"),

    /** 周期数据已提交 */
    PERIOD_ALREADY_SUBMITTED(4007, "该周期数据已提交，请先撤回"),

    /** 文件上传失败 */
    UPLOAD_FAILED(5001, "文件上传失败"),

    /** 文件类型不支持 */
    FILE_TYPE_NOT_SUPPORTED(5002, "文件类型不支持"),

    /** 文件大小超限 */
    FILE_SIZE_EXCEEDED(5003, "文件大小超出限制");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
