package com.groupreport.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 数据源配置DTO
 */
@Data
@Schema(description = "数据源配置请求")
public class DataSourceDTO {

    @Schema(description = "数据源ID（更新时必填）")
    private Long id;

    @NotBlank(message = "数据源编码不能为空")
    @Schema(description = "数据源编码，如：ds_mysql_prod")
    private String sourceCode;

    @NotBlank(message = "数据源名称不能为空")
    @Schema(description = "数据源名称")
    private String sourceName;

    @NotBlank(message = "数据源类型不能为空")
    @Schema(description = "数据源类型：mysql/postgresql/api/elasticsearch/file/excel")
    private String sourceType;

    @Schema(description = "连接配置JSON")
    private Map<String, Object> connectionConfig;

    @Schema(description = "SQL查询或API路径")
    private String queryTemplate;

    @Schema(description = "字段映射配置")
    private Map<String, String> fieldMapping;

    @Schema(description = "刷新策略：manual/auto_5min/auto_1hour/daily/cron")
    private String refreshPolicy = "manual";

    @Schema(description = "Cron表达式")
    private String cronExpression;

    @Schema(description = "缓存过期时间（秒），0不缓存")
    private Long cacheTtl = 0L;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用连接池")
    private Boolean usePool = false;

    @Schema(description = "最大连接数")
    private Integer maxPoolSize = 10;

    @Schema(description = "超时时间（毫秒）")
    private Integer timeoutMs = 30000;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status = 1;
}
