package com.groupreport.platform.common;

/**
 * 常量定义
 */
public final class Constants {

    private Constants() {}

    /** 默认密码 */
    public static final String DEFAULT_PASSWORD = "admin123";

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 缓存前缀 */
    public static final String CACHE_PREFIX_USER = "user:";
    public static final String CACHE_PREFIX_TEMPLATE = "template:";
    public static final String CACHE_PREFIX_ORG = "org:";

    /** 超级管理员ID */
    public static final Long SUPER_ADMIN_ID = 1L;

    /** 状态常量 */
    public static final class Status {
        public static final int DISABLED = 0;
        public static final int ENABLED = 1;
    }

    /** 删除标记 */
    public static final class Deleted {
        public static final int NO = 0;
        public static final int YES = 1;
    }

    /** 组织类型 */
    public static final class OrgType {
        public static final int GROUP = 1;      // 集团
        public static final int SUB_COMPANY = 2; // 子公司
        public static final int DEPARTMENT = 3;  // 部门
        public static final int TEAM = 4;        // 小组
    }

    /** 模板类型 */
    public static final class TemplateType {
        public static final int STATISTICS = 1;  // 统计报表
        public static final int FILL_IN = 2;     // 填报报表
        public static final int SUMMARY = 3;     // 汇总报表
    }

    /** 模板状态 */
    public static final class TemplateStatus {
        public static final int DRAFT = 0;       // 草稿
        public static final int PUBLISHED = 1;   // 已发布
        public static final int DISABLED = 2;    // 已停用
    }

    /** 周期类型 */
    public static final class PeriodType {
        public static final int DAILY = 1;   // 日
        public static final int WEEKLY = 2;  // 周
        public static final int MONTHLY = 3; // 月
        public static final int QUARTERLY = 4;// 季
        public static final int YEARLY = 5;  // 年
    }

    /** 提交状态 */
    public static final class SubmitStatus {
        public static final int DRAFT = 0;      // 草稿
        public static final int PENDING = 1;    // 待审核
        public static final int APPROVED = 2;   // 已通过
        public static final int REJECTED = 3;   // 已驳回
        public static final int WITHDRAWN = 4;  // 已撤回
    }

    /** 审核类型 */
    public static final class AuditType {
        public static final int SUBMIT = 1;      // 提交审核
        public static final int APPROVE = 2;     // 审批通过
        public static final int REJECT = 3;      // 审批驳回
        public static final int WITHDRAW = 4;    // 撤回
        public static final int RESUBMIT = 5;    // 重新提交
    }

    /** 行/列类型 */
    public static final class RowColumnType {
        public static final int TEXT = 1;        // 文本
        public static final int NUMBER = 2;      // 数字
        public static final int DATE = 3;        // 日期
        public static final int SELECT = 4;      // 下拉选择
        public static final int FORMULA = 5;     // 公式
        public static final int READONLY = 6;    // 只读
    }

    /** 数据来源 */
    public static final class DataSource {
        public static final int MANUAL = 1;      // 手动录入
        public static final int FORMULA = 2;     // 公式计算
        public static final int IMPORT = 3;      // 系统导入
        public static final int API = 4;         // 接口同步
    }

    /** 数据范围 */
    public static final class DataScope {
        public static final int ALL = 1;             // 全部数据
        public static final int DEPT_AND_BELOW = 2;  // 本部门及以下
        public static final int ONLY_DEPT = 3;       // 仅本部门
        public static final int ONLY_SELF = 4;       // 仅本人
    }
}
