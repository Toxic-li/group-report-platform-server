-- =====================================================
-- 集团统计报表平台 - 数据库初始化脚本
-- GroupReportPlatform Database Initialization Script
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

CREATE DATABASE IF NOT EXISTS `group_report_db`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `group_report_db`;

-- =====================================================
-- 1. 组织机构表 sys_org
-- =====================================================
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID，0为顶级',
    `org_code` VARCHAR(64) NOT NULL COMMENT '组织编码（唯一）',
    `org_name` VARCHAR(128) NOT NULL COMMENT '组织名称',
    `org_type` TINYINT NOT NULL DEFAULT 1 COMMENT '组织类型：1-集团 2-子公司 3-部门 4-小组',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `leader` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `address` VARCHAR(256) DEFAULT NULL COMMENT '地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `tree_path` VARCHAR(1024) DEFAULT NULL COMMENT '树路径，如：0,1,2,3',
    `level` INT NOT NULL DEFAULT 1 COMMENT '层级，从1开始',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_org_code` (`org_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_tree_path` (`tree_path`(255)),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织机构表';

-- =====================================================
-- 2. 用户表 sys_user
-- =====================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名（唯一）',
    `password` VARCHAR(128) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `org_id` BIGINT NOT NULL COMMENT '所属组织ID',
    `position` VARCHAR(64) DEFAULT NULL COMMENT '职位',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_status` (`status`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 3. 角色表 sys_role
-- =====================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码（唯一）',
    `role_name` VARCHAR(128) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '角色描述',
    `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据范围：1-全部 2-本部门及以下 3-本部门 4-本人',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =====================================================
-- 4. 用户角色关联表 sys_user_role
-- =====================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =====================================================
-- 5. 报表模板表 rpt_template
-- =====================================================
DROP TABLE IF EXISTS `rpt_template`;
CREATE TABLE `rpt_template` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码（唯一）',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `template_type` TINYINT NOT NULL DEFAULT 1 COMMENT '模板类型：1-统计报表 2-填报报表 3-汇总报表',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `description` TEXT COMMENT '模板描述',
    `version` INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已停用',
    `is_public` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公开：0-否 1-是',
    `allow_export` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许导出：0-否 1-是',
    `allow_import` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许导入：0-否 1-是',
    `period_type` TINYINT NOT NULL DEFAULT 1 COMMENT '周期类型：1-日 2-周 3-月 4-季 5-年',
    `submit_deadline` INT DEFAULT NULL COMMENT '填报截止时间（小时）',
    `audit_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审核：0-否 1-是',
    `formula_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用公式：0-否 1-是',
    `row_count` INT DEFAULT 0 COMMENT '行数',
    `column_count` INT DEFAULT 0 COMMENT '列数',
    `config_json` JSON COMMENT '扩展配置JSON',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_template_type` (`template_type`),
    KEY `idx_status` (`status`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表模板表';

-- =====================================================
-- 6. 模板行结构表 rpt_template_row
-- =====================================================
DROP TABLE IF EXISTS `rpt_template_row`;
CREATE TABLE `rpt_template_row` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `row_code` VARCHAR(64) NOT NULL COMMENT '行编码（同一模板内唯一）',
    `row_name` VARCHAR(128) NOT NULL COMMENT '行名称/标题',
    `row_type` TINYINT NOT NULL DEFAULT 1 COMMENT '行类型：1-数据行 2-标题行 3-合计行 4-分组头',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父行ID，0表示无父行',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `level` INT NOT NULL DEFAULT 1 COMMENT '层级',
    `indent` INT DEFAULT 0 COMMENT '缩进级别',
    `is_expandable` TINYINT NOT NULL DEFAULT 0 COMMENT '是否可展开：0-否 1-是',
    `is_summary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否合计行：0-否 1-是',
    `summary_formula` VARCHAR(512) DEFAULT NULL COMMENT '合计公式',
    `background_color` VARCHAR(32) DEFAULT NULL COMMENT '背景颜色',
    `font_bold` TINYINT NOT NULL DEFAULT 0 COMMENT '是否加粗：0-否 1-是',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
    `frozen` TINYINT NOT NULL DEFAULT 0 COMMENT '是否冻结：0-否 1-是',
    `height` INT DEFAULT 30 COMMENT '行高（像素）',
    `config_json` JSON COMMENT '扩展配置',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_row` (`template_id`, `row_code`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板行结构表';

-- =====================================================
-- 7. 模板列结构表 rpt_template_column
-- =====================================================
DROP TABLE IF EXISTS `rpt_template_column`;
CREATE TABLE `rpt_template_column` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `column_code` VARCHAR(64) NOT NULL COMMENT '列编码（同一模板内唯一）',
    `column_name` VARCHAR(128) NOT NULL COMMENT '列名称/标题',
    `column_type` TINYINT NOT NULL DEFAULT 1 COMMENT '列类型：1-文本 2-数字 3-日期 4-下拉选择 5-公式 6-只读',
    `data_type` TINYINT NOT NULL DEFAULT 1 COMMENT '数据类型：1-字符串 2-整数 3-小数 4-百分比 5-金额',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父列ID，0表示无父列',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
    `width` INT DEFAULT 100 COMMENT '列宽（像素）',
    `decimal_places` INT DEFAULT 2 COMMENT '小数位数',
    `unit` VARCHAR(32) DEFAULT NULL COMMENT '单位',
    `required` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填：0-否 1-是',
    `readonly` TINYINT NOT NULL DEFAULT 0 COMMENT '是否只读：0-否 1-是',
    `default_value` VARCHAR(256) DEFAULT NULL COMMENT '默认值',
    `options_json` JSON COMMENT '选项配置（下拉选择等）',
    `min_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最小值',
    `max_value` DECIMAL(20,6) DEFAULT NULL COMMENT '最大值',
    `format_pattern` VARCHAR(64) DEFAULT NULL COMMENT '格式化模式',
    `background_color` VARCHAR(32) DEFAULT NULL COMMENT '背景颜色',
    `font_bold` TINYINT NOT NULL DEFAULT 0 COMMENT '是否加粗',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见',
    `frozen` TINYINT NOT NULL DEFAULT 0 COMMENT '是否冻结',
    `align` TINYINT DEFAULT 0 COMMENT '对齐方式：0-左 1-中 2-右',
    `config_json` JSON COMMENT '扩展配置',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_column` (`template_id`, `column_code`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板列结构表';

-- =====================================================
-- 8. 公式表 rpt_formula
-- =====================================================
DROP TABLE IF EXISTS `rpt_formula`;
CREATE TABLE `rpt_formula` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `formula_name` VARCHAR(128) NOT NULL COMMENT '公式名称',
    `formula_expression` TEXT NOT NULL COMMENT '公式表达式',
    `target_row_code` VARCHAR(64) NOT NULL COMMENT '目标行编码',
    `target_column_code` VARCHAR(64) NOT NULL COMMENT '目标列编码',
    `formula_type` TINYINT NOT NULL DEFAULT 1 COMMENT '公式类型：1-求和 2-平均值 3-最大值 4-最小值 5-自定义表达式',
    `source_range` VARCHAR(256) DEFAULT NULL COMMENT '源数据范围，如：R1C1:R10C5',
    `calc_trigger` TINYINT NOT NULL DEFAULT 1 COMMENT '计算触发：1-实时 2-保存时 3-提交时',
    `priority` INT DEFAULT 0 COMMENT '优先级，数值越小越先计算',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`),
    KEY `idx_target` (`target_row_code`, `target_column_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公式表';

-- =====================================================
-- 9. 校验规则表 rpt_validator
-- =====================================================
DROP TABLE IF EXISTS `rpt_validator`;
CREATE TABLE `rpt_validator` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `validator_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
    `validator_type` TINYINT NOT NULL DEFAULT 1 COMMENT '校验类型：1-非空 2-范围 3-正则 4-自定义 5-业务规则',
    `target_rows` VARCHAR(1024) DEFAULT NULL COMMENT '目标行编码列表，逗号分隔',
    `target_columns` VARCHAR(1024) DEFAULT NULL COMMENT '目标列编码列表，逗号分隔',
    `rule_config` JSON NOT NULL COMMENT '规则配置JSON',
    `error_message` VARCHAR(512) NOT NULL COMMENT '错误提示信息',
    `validate_trigger` TINYINT NOT NULL DEFAULT 1 COMMENT '校验时机：1-输入时 2-保存时 3-提交时',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校验规则表';

-- =====================================================
-- 10. 模板版本表 rpt_template_version
-- =====================================================
DROP TABLE IF EXISTS `rpt_template_version`;
CREATE TABLE `rpt_template_version` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `version_number` INT NOT NULL COMMENT '版本号',
    `version_name` VARCHAR(128) DEFAULT NULL COMMENT '版本名称',
    `change_log` TEXT COMMENT '变更说明',
    `snapshot_json` JSON COMMENT '快照数据（完整模板配置）',
    `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-未发布 1-已发布 2-已回滚',
    `published_by` BIGINT DEFAULT NULL COMMENT '发布人ID',
    `published_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_version` (`template_id`, `version_number`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板版本表';

-- =====================================================
-- 11. 报表提交表 rpt_submit
-- =====================================================
DROP TABLE IF EXISTS `rpt_submit`;
CREATE TABLE `rpt_submit` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `submit_no` VARCHAR(64) NOT NULL COMMENT '提交编号（唯一）',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `org_id` BIGINT NOT NULL COMMENT '填报组织ID',
    `period` VARCHAR(32) NOT NULL COMMENT '填报周期，如：202401,2024Q1',
    `period_type` TINYINT NOT NULL DEFAULT 1 COMMENT '周期类型：1-日 2-周 3-月 4-季 5-年',
    `submit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '提交状态：0-草稿 1-待审核 2-已通过 3-已驳回 4-已撤回',
    `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `audit_remark` VARCHAR(1024) DEFAULT NULL COMMENT '审核意见',
    `data_complete_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '数据完整率',
    `total_rows` INT DEFAULT 0 COMMENT '总行数',
    `filled_rows` INT DEFAULT 0 COMMENT '已填写行数',
    `attachment_url` VARCHAR(512) DEFAULT NULL COMMENT '附件地址',
    `remark` VARCHAR(1024) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_by` BIGINT NOT NULL COMMENT '创建人ID（填报人）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_submit_no` (`submit_no`),
    KEY `idx_template_org_period` (`template_id`, `org_id`, `period`),
    KEY `idx_submit_status` (`submit_status`),
    KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表提交表';

-- =====================================================
-- 12. 报表数据表 rpt_data（通用数据存储）
-- =====================================================
DROP TABLE IF EXISTS `rpt_data`;
CREATE TABLE `rpt_data` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `submit_id` BIGINT DEFAULT NULL COMMENT '提交记录ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `org_id` BIGINT NOT NULL COMMENT '组织ID',
    `period` VARCHAR(32) NOT NULL COMMENT '周期',
    `row_id` BIGINT NOT NULL COMMENT '行结构ID',
    `row_code` VARCHAR(64) NOT NULL COMMENT '行编码',
    `column_id` BIGINT NOT NULL COMMENT '列结构ID',
    `column_code` VARCHAR(64) NOT NULL COMMENT '列编码',
    `value_text` VARCHAR(2048) DEFAULT NULL COMMENT '文本值',
    `value_number` DECIMAL(20,6) DEFAULT NULL COMMENT '数值',
    `value_date` DATE DEFAULT NULL COMMENT '日期值',
    `data_type` TINYINT NOT NULL DEFAULT 1 COMMENT '数据类型：1-文本 2-数字 3-日期',
    `is_formula` TINYINT NOT NULL DEFAULT 0 COMMENT '是否公式计算值：0-否 1-是',
    `is_modified` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被修改：0-否 1-是',
    `source` TINYINT NOT NULL DEFAULT 1 COMMENT '数据来源：1-手动录入 2-公式计算 3-系统导入 4-接口同步',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_data_cell` (`template_id`, `org_id`, `period`, `row_code`, `column_code`),
    KEY `idx_submit_id` (`submit_id`),
    KEY `idx_template_org_period` (`template_id`, `org_id`, `period`),
    KEY `idx_row_column` (`row_code`, `column_code`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报表数据表';

-- =====================================================
-- 13. 数据历史表 rpt_data_history
-- =====================================================
DROP TABLE IF EXISTS `rpt_data_history`;
CREATE TABLE `rpt_data_history` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `data_id` BIGINT NOT NULL COMMENT '原数据ID',
    `submit_id` BIGINT DEFAULT NULL COMMENT '提交记录ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `org_id` BIGINT NOT NULL COMMENT '组织ID',
    `period` VARCHAR(32) NOT NULL COMMENT '周期',
    `row_code` VARCHAR(64) NOT NULL COMMENT '行编码',
    `column_code` VARCHAR(64) NOT NULL COMMENT '列编码',
    `old_value_text` VARCHAR(2048) DEFAULT NULL COMMENT '旧文本值',
    `old_value_number` DECIMAL(20,6) DEFAULT NULL COMMENT '旧数值',
    `new_value_text` VARCHAR(2048) DEFAULT NULL COMMENT '新文本值',
    `new_value_number` DECIMAL(20,6) DEFAULT NULL COMMENT '新数值',
    `change_type` TINYINT NOT NULL DEFAULT 1 COMMENT '变更类型：1-新增 2-修改 3-删除',
    `operate_by` BIGINT NOT NULL COMMENT '操作人ID',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_data_id` (`data_id`),
    KEY `idx_submit_id` (`submit_id`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据历史表';

-- =====================================================
-- 14. 审核记录表 rpt_audit_log
-- =====================================================
DROP TABLE IF EXISTS `rpt_audit_log`;
CREATE TABLE `rpt_audit_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `submit_id` BIGINT NOT NULL COMMENT '提交记录ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `org_id` BIGINT NOT NULL COMMENT '组织ID',
    `audit_type` TINYINT NOT NULL DEFAULT 1 COMMENT '审核类型：1-提交审核 2-审批通过 3-审批驳回 4-撤回 5-重新提交',
    `from_status` TINYINT DEFAULT NULL COMMENT '原状态',
    `to_status` TINYINT NOT NULL COMMENT '目标状态',
    `auditor_id` BIGINT NOT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(64) DEFAULT NULL COMMENT '审核人姓名',
    `audit_result` TINYINT NOT NULL COMMENT '审核结果：0-拒绝 1-通过',
    `audit_opinion` VARCHAR(2048) DEFAULT NULL COMMENT '审核意见',
    `audit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    `attachment_url` VARCHAR(512) DEFAULT NULL COMMENT '附件',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_submit_id` (`submit_id`),
    KEY `idx_auditor_id` (`auditor_id`),
    KEY `idx_audit_time` (`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- =====================================================
-- 15. 条件格式表 rpt_conditional_format（V2设计器新增）
-- =====================================================
DROP TABLE IF EXISTS `rpt_conditional_format`;
CREATE TABLE `rpt_conditional_format` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `format_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
    `condition_type` TINYINT NOT NULL DEFAULT 1 COMMENT '条件类型：1-单元格值 2-公式 3-前N名 4-重复值 5-空值',
    `operator` VARCHAR(32) DEFAULT NULL COMMENT '运算符：eq/ne/gt/ge/lt/le/between/contains',
    `condition_value` JSON DEFAULT NULL COMMENT '条件值1（JSON存储）',
    `condition_value2` JSON DEFAULT NULL COMMENT '条件值2（between时使用）',
    `apply_range` VARCHAR(512) DEFAULT NULL COMMENT '应用范围，如：R1C1:R10C5',
    `style_config` JSON DEFAULT NULL COMMENT '样式配置JSON：backgroundColor/color/bold/italic/fontSize',
    `stop_if_true` TINYINT NOT NULL DEFAULT 0 COMMENT '停用后续规则',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='条件格式表';

-- =====================================================
-- 16. 数据源配置表 sys_data_source（V2设计器新增）
-- =====================================================
DROP TABLE IF EXISTS `sys_data_source`;
CREATE TABLE `sys_data_source` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `source_code` VARCHAR(64) NOT NULL COMMENT '数据源编码',
    `source_name` VARCHAR(128) NOT NULL COMMENT '数据源名称',
    `source_type` VARCHAR(32) NOT NULL COMMENT '数据源类型：mysql/postgresql/api/elasticsearch/file/excel',
    `connection_config` JSON DEFAULT NULL COMMENT '连接配置JSON',
    `query_template` TEXT DEFAULT NULL COMMENT 'SQL查询语句或API路径',
    `field_mapping` JSON DEFAULT NULL COMMENT '字段映射配置JSON',
    `refresh_policy` VARCHAR(32) DEFAULT 'manual' COMMENT '刷新策略：manual/auto_5min/auto_1hour/daily/cron',
    `cron_expression` VARCHAR(128) DEFAULT NULL COMMENT 'Cron表达式',
    `cache_ttl` BIGINT DEFAULT 0 COMMENT '缓存过期时间(秒)，0不缓存',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `use_pool` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用连接池',
    `max_pool_size` INT DEFAULT 10 COMMENT '最大连接数',
    `timeout_ms` INT DEFAULT 30000 COMMENT '超时时间(ms)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_source_code` (`source_code`),
    KEY `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据源配置表';

-- =====================================================
-- 示例数据初始化
-- =====================================================

-- 组织机构示例数据
INSERT INTO `sys_org` (`id`, `parent_id`, `org_code`, `org_name`, `org_type`, `sort_order`, `leader`, `phone`, `status`, `tree_path`, `level`) VALUES
(1, 0, 'GROUP_001', '某某集团有限公司', 1, 1, '张总', '13800138000', 1, '0,1', 1),
(2, 1, 'SUB_001', '北京子公司', 2, 1, '李经理', '13800138001', 1, '0,1,2', 2),
(3, 1, 'SUB_002', '上海子公司', 2, 2, '王经理', '13800138002', 1, '0,1,3', 2),
(4, 2, 'DEPT_001', '北京子公司-财务部', 3, 1, '赵主管', '13800138003', 1, '0,1,2,4', 3),
(5, 2, 'DEPT_002', '北京子公司-人事部', 3, 2, '钱主管', '13800138004', 1, '0,1,2,5', 3),
(6, 3, 'DEPT_003', '上海子公司-财务部', 3, 1, '孙主管', '13800138005', 1, '0,1,3,6', 3),
(7, 3, 'DEPT_004', '上海子公司-市场部', 3, 2, '周主管', '13800138006', 1, '0,1,3,7', 3);

-- 角色示例数据
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `sort_order`, `status`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有系统所有权限', 1, 1, 1),
(2, 'ADMIN', '管理员', '拥有管理权限', 2, 2, 1),
(3, 'AUDITOR', '审核员', '负责报表审核', 3, 3, 1),
(4, 'REPORTER', '填报员', '负责数据填报', 4, 4, 1),
(5, 'VIEWER', '查看者', '只读权限', 4, 5, 1);

-- 用户示例数据（密码为 admin123 的BCrypt加密结果）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `nickname`, `email`, `phone`, `org_id`, `position`, `status`) VALUES
(1, 'admin', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', '超级管理员', 'Admin', 'admin@group.com', '13800138000', 1, '系统管理员', 1),
(2, 'zhangsan', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', '张三', '张三', 'zhangsan@group.com', '13800138001', 4, '财务专员', 1),
(3, 'lisi', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', '李四', '李四', 'lisi@group.com', '13800138002', 6, '财务专员', 1),
(4, 'wangwu', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', '王五', '王五', 'wangwu@group.com', '13800138003', 5, 'HR专员', 1),
(5, 'auditor', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGM/TEZyj1KO', '审核员', 'Auditor', 'auditor@group.com', '13800138004', 1, '审核员', 1);

-- 用户角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1), (2, 1, 2),
(3, 2, 4),
(4, 3, 4),
(5, 4, 4),
(6, 5, 3);

-- 报表模板示例数据
INSERT INTO `rpt_template` (`id`, `template_code`, `template_name`, `template_type`, `description`, `version`, `status`, `period_type`, `audit_required`, `row_count`, `column_count`) VALUES
(1, 'MONTHLY_FINANCE', '月度财务报表', 1, '各子公司月度财务数据统计报表', 1, 1, 3, 1, 8, 6),
(2, 'QUARTERLY_SALES', '季度销售报表', 1, '各子公司季度销售业绩统计', 1, 1, 4, 1, 6, 5),
(3, 'YEARLY_HR', '年度人力资源报表', 1, '年度人力资源相关数据统计', 1, 1, 5, 1, 10, 7);

-- 月度财务报表 - 行结构
INSERT INTO `rpt_template_row` (`id`, `template_id`, `row_code`, `row_name`, `row_type`, `parent_id`, `sort_order`, `level`, `is_summary`) VALUES
(1, 1, 'TITLE', '月度财务报表', 2, 0, 1, 1, 0),
(2, 1, 'INCOME', '一、营业收入', 4, 0, 2, 1, 0),
(3, 1, 'SALES_REV', '  主营业务收入', 1, 2, 3, 2, 0),
(4, 1, 'OTHER_REV', '  其他业务收入', 1, 2, 4, 2, 0),
(5, 1, 'COST', '二、营业成本', 4, 0, 5, 1, 0),
(6, 1, 'MAIN_COST', '  主营业务成本', 1, 5, 6, 2, 0),
(7, 1, 'OTHER_COST', '  其他业务成本', 1, 5, 7, 2, 0),
(8, 1, 'PROFIT', '三、营业利润', 3, 0, 8, 1, 1);

-- 月度财务报表 - 列结构
INSERT INTO `rpt_template_column` (`id`, `template_id`, `column_code`, `column_name`, `column_type`, `data_type`, `sort_order`, `width`, `unit`, `decimal_places`) VALUES
(1, 1, 'ITEM', '项目', 6, 1, 1, 200, NULL, 0),
(2, 1, 'CURRENT_MONTH', '本月数', 1, 3, 2, 150, '元', 2),
(3, 1, 'LAST_MONTH', '上月数', 1, 3, 3, 150, '元', 2),
(4, 1, 'CURRENT_YEAR', '本年累计', 1, 3, 4, 150, '元', 2),
(5, 1, 'LAST_YEAR', '上年同期', 1, 3, 5, 150, '元', 2),
(6, 1, 'YOY_CHANGE', '同比变动%', 5, 4, 6, 120, '%', 2);

-- 季度销售报表 - 行结构
INSERT INTO `rpt_template_row` (`id`, `template_id`, `row_code`, `row_name`, `row_type`, `parent_id`, `sort_order`, `level`, `is_summary`) VALUES
(101, 2, 'TITLE', '季度销售报表', 2, 0, 1, 1, 0),
(102, 2, 'REGION_EAST', '华东区域', 4, 0, 2, 1, 0),
(103, 2, 'PRODUCT_A_EAST', '  产品A销售额', 1, 102, 3, 2, 0),
(104, 2, 'PRODUCT_B_EAST', '  产品B销售额', 1, 102, 4, 2, 0),
(105, 2, 'REGION_WEST', '华西区域', 4, 0, 5, 1, 0),
(106, 2, 'TOTAL_SALES', '销售合计', 3, 0, 6, 1, 1);

-- 季度销售报表 - 列结构
INSERT INTO `rpt_template_column` (`id`, `template_id`, `column_code`, `column_name`, `column_type`, `data_type`, `sort_order`, `width`, `unit`) VALUES
(201, 2, 'ITEM', '项目', 6, 1, 1, 180, NULL),
(202, 2, 'Q1', 'Q1', 1, 3, 2, 120, '万元'),
(203, 2, 'Q2', 'Q2', 1, 3, 3, 120, '万元'),
(204, 2, 'Q3', 'Q3', 1, 3, 4, 120, '万元'),
(205, 2, 'Q4', 'Q4', 1, 3, 5, 120, '万元');

-- 公式示例数据
INSERT INTO `rpt_formula` (`id`, `template_id`, `formula_name`, `formula_expression`, `target_row_code`, `target_column_code`, `formula_type`, `calc_trigger`) VALUES
(1, 1, '营业利润计算', 'SUM(R[INCOME],C[CURRENT_MONTH]) - SUM(R[COST],C[CURRENT_MONTH])', 'PROFIT', 'CURRENT_MONTH', 1, 2);

-- 校验规则示例数据
INSERT INTO `rpt_validator` (`id`, `template_id`, `validator_name`, `validator_type`, `target_columns`, `rule_config`, `error_message`, `validate_trigger`) VALUES
(1, 1, '金额非负校验', 2, 'CURRENT_MONTH,LAST_MONTH,CURRENT_YEAR,LAST_YEAR', '{"min": 0}', '金额不能为负数', 3),
(2, 1, '必填项校验', 1, 'CURRENT_MONTH,CURRENT_YEAR', '{"required": true}', '本月数和本年累计为必填项', 3);
