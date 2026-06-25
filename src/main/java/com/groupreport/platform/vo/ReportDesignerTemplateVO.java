package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 报表设计器统一模板JSON — 低代码设计器V2 核心数据结构
 * 前端设计器 ↔ 后端引擎 之间传输的完整模板定义
 */
@Data
@Schema(description = "报表设计器完整模板JSON")
public class ReportDesignerTemplateVO {

    // ==================== 模板元信息 ====================

    @Schema(description = "模板ID")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "模板编码")
    private String code;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "状态：draft-草稿 published-已发布 disabled-已停用")
    private String status;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "周期类型：day/week/month/quarter/year")
    private String periodType;

    @Schema(description = "是否需要审核")
    private Boolean auditRequired;

    // ==================== 布局配置 ====================

    @Schema(description = "布局配置")
    private LayoutConfig layout;

    @Data
    public static class LayoutConfig {
        @Schema(description = "布局类型：table/pivot/chart/mixed")
        private String type = "table";

        @Schema(description = "是否显示行号列")
        private Boolean showRowHeader = true;

        @Schema(description = "是否显示列头行")
        private Boolean showColumnHeader = true;

        @Schema(description = "冻结行数")
        private Integer freezeRows = 0;

        @Schema(description = "冻结列数")
        private Integer freezeCols = 0;

        @Schema(description = "默认行高")
        private Integer defaultRowHeight = 32;

        @Schema(description = "默认列宽")
        private Integer defaultColWidth = 120;
    }

    // ==================== 行树结构 ====================

    @Schema(description = "行树结构（支持无限层级）")
    private List<TreeNode> rowTree;

    // ==================== 列树结构 ====================

    @Schema(description = "列树结构（支持无限层级）")
    private List<TreeNode> columnTree;

    // ==================== 度量指标/公式 ====================

    @Schema(description = "度量指标列表（公式定义）")
    private List<MetricDef> metrics;

    @Data
    public static class MetricDef {
        @Schema(description = "字段标识，如：q1Rate")
        private String field;

        @Schema(description = "显示标签，如：Q1完成率")
        private String label;

        @Schema(description = "公式表达式，如：q1Done / q1Plan")
        private String expression;

        @Schema(description = "结果类型：number/string/percent/currency/date")
        private String resultType;

        @Schema(description = "目标单元格位置，如：3-5")
        private String targetCell;

        @Schema(description = "依赖字段列表")
        private List<String> dependencies;

        @Schema(description = "计算触发：realtime/save/submit")
        private String calcTrigger = "save";

        @Schema(description = "优先级")
        private Integer priority = 0;

        @Schema(description = "格式化模式，如：0.00%, #,##0.00")
        private String formatPattern;

        @Schema(description = "描述")
        private String description;
    }

    // ==================== 合计规则 ====================

    @Schema(description = "合计规则列表")
    private List<AggregateDef> aggregates;

    @Data
    public static class AggregateDef {
        @Schema(description = "目标行编码")
        private String targetRowCode;

        @Schema(description = "聚合方式：sum/avg/max/min/count")
        private String method;

        @Schema(description = "源范围，如：R1C1:R10C5 或 * 表示全部数据列")
        private String sourceRange;

        @Schema(description = "自定义公式（method=custom时使用）")
        private String customFormula;

        @Schema(description = "显示标签")
        private String label;
    }

    // ==================== 校验规则 ====================

    @Schema(description = "校验规则列表")
    private List<ValidatorDef> validators;

    @Data
    public static class ValidatorDef {
        @Schema(description = "规则名称")
        private String name;

        @Schema(description = "校验类型：not_null/range/regex/custom/business")
        private String type;

        @Schema(description = "目标行编码列表")
        private List<String> targetRows;

        @Schema(description = "目标列编码列表")
        private List<String> targetColumns;

        @Schema(description = "规则配置（根据type不同含义不同）")
        private Map<String, Object> ruleConfig;

        @Schema(description = "错误提示信息")
        private String errorMessage;

        @Schema(description = "校验时机：input/save/submit")
        private String validateTrigger = "save";

        @Schema(description = "优先级")
        private Integer priority = 0;
    }

    // ==================== 条件格式 ====================

    @Schema(description = "条件格式规则列表")
    private List<ConditionalFormatDef> conditionalFormats;

    @Data
    public static class ConditionalFormatDef {
        @Schema(description = "规则名称")
        private String name;

        @Schema(description = "条件类型：cell_value/formula/top_bottom/duplicate/blank")
        private String conditionType;

        @Schema(description = "运算符：eq/ne/gt/ge/lt/le/between/contains")
        private String operator;

        @Schema(description = "条件值（可以是数值或公式引用）")
        private Object value;

        @Schema(description = "条件值2（between时使用）")
        private Object value2;

        @Schema(description = "应用范围，如：R1C1:R10C5 或 [\"R1C1\", \"R2C2\"]")
        private String applyRange;

        @Schema(description = "样式配置")
        private StyleConfig style;

        @Schema(description = "停止处理后续规则")
        private Boolean stopIfTrue;

        @Data
        public static class StyleConfig {
            @Schema(description = "背景色，如：#FF0000")
            private String backgroundColor;

            @Schema(description = "前景色/字体颜色")
            private String color;

            @Schema(description = "是否加粗")
            private Boolean bold;

            @Schema(description = "是否斜体")
            private Boolean italic;

            @Schema(description = "字体大小")
            private Integer fontSize;
        }
    }

    // ==================== 数据源配置 ====================

    @Schema(description = "数据源配置")
    private DataSourceDef dataSource;

    @Data
    public static class DataSourceDef {
        @Schema(description = "数据源类型：mysql/api/elasticsearch/file")
        private String type = "mysql";

        @Schema(description = "数据源ID或连接标识")
        private String sourceId;

        @Schema(description = "数据源名称")
        private String sourceName;

        @Schema(description = "SQL查询语句或API路径")
        private String query;

        @Schema(description = "字段映射配置")
        private Map<String, String> fieldMapping;

        @Schema(description = "刷新策略：manual/auto_5min/auto_1hour/daily")
        private String refreshPolicy = "manual";
    }

    // ==================== 扩展配置 ====================

    @Schema(description = "权限配置")
    private PermissionConfig permissions;

    @Schema(description = "单元格数据 Map<rowCode:columnCode, value>")
    private Map<String, String> cellData;

    @Data
    public static class PermissionConfig {
        @Schema(description = "可编辑的组织ID列表")
        private List<Long> editableOrgs;

        @Schema(description = "可查看的组织ID列表")
        private List<Long> viewableOrgs;

        @Schema(description = "可导出")
        private Boolean allowExport = false;

        @Schema(description = "可导入")
        private Boolean allowImport = false;
    }

    @Schema(description = "扩展属性（预留）")
    private Map<String, Object> extra;

    // ==================== 通用树节点 ====================

    /**
     * 行/列通用树节点结构
     */
    @Data
    public static class TreeNode {
        @Schema(description = "节点编码")
        private String id;

        @Schema(description = "节点名称")
        private String name;

        @Schema(description = "节点类型：data/header/summary/group")
        private String type;

        @Schema(description = "父节点ID")
        private String parentId;

        @Schema(description = "排序号")
        private Integer sortOrder;

        @Schema(description = "层级深度")
        private Integer level;

        @Schema(description = "子节点列表")
        private List<TreeNode> children;

        /** --- 行节点特有属性 --- */
        @Schema(description = "是否可展开")
        private Boolean expandable;

        @Schema(description = "是否合计行")
        private Boolean isSummary;

        @Schema(description = "行高")
        private Integer height;

        @Schema(description = "背景颜色")
        private String backgroundColor;

        /** --- 列节点特有属性 --- */
        @Schema(description = "列宽")
        private Integer width;

        @Schema(description = "列类型：text/number/date/select/formula/readonly")
        private String columnType;

        @Schema(description = "数据类型：string/integer/decimal/percent/currency")
        private String dataType;

        @Schema(description = "小数位数")
        private Integer decimalPlaces;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "是否必填")
        private Boolean required;

        @Schema(description = "是否只读")
        private Boolean readonly;

        @Schema(description = "默认值")
        private String defaultValue;

        @Schema(description = "选项配置（下拉选择时）")
        private List<OptionItem> options;

        @Schema(description = "最小值")
        private Number minValue;

        @Schema(description = "最大值")
        private Number maxValue;

        @Schema(description = "格式化模式")
        private String formatPattern;

        @Schema(description = "对齐方式：left/center/right")
        private String align;

        @Schema(description = "是否可见")
        private Boolean visible = true;

        @Schema(description = "是否冻结")
        private Boolean frozen = false;

        @Schema(description = "扩展配置")
        private Map<String, Object> config;
    }

    @Data
    public static class OptionItem {
        private String value;
        private String label;
    }

    // ==================== 预览结果 ====================

    @Data
    public static class PreviewResult {
        @Schema(description = "模板定义")
        private ReportDesignerTemplateVO template;

        @Schema(description = "单元格数据 Map<rowCode:columnCode, value>")
        private Map<String, String> cellData;

        @Schema(description = "公式计算结果")
        private Map<String, String> formulaResults;

        @Schema(description = "校验错误")
        private Map<String, String> validationErrors;

        @Schema(description = "条件格式应用结果")
        private List<FormatCell> formatCells;

        @Data
        public static class FormatCell {
            private String cellKey;
            private Object style;
        }
    }
}
