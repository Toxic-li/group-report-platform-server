package com.groupreport.platform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据源配置VO
 */
@Data
@Schema(description = "数据源信息")
public class DataSourceVO {

    @Schema(description = "数据源ID")
    private Long id;

    @Schema(description = "数据源编码")
    private String sourceCode;

    @Schema(description = "数据源名称")
    private String sourceName;

    @Schema(description = "数据源类型")
    private String sourceType;

    @Schema(description = "连接配置（脱敏）")
    private Map<String, Object> connectionConfig;

    @Schema(description = "SQL查询或API路径")
    private String queryTemplate;

    @Schema(description = "字段映射")
    private Map<String, String> fieldMapping;

    @Schema(description = "刷新策略")
    private String refreshPolicy;

    @Schema(description = "缓存过期时间(秒)")
    private Long cacheTtl;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用连接池")
    private Boolean usePool;

    @Schema(description = "最大连接数")
    private Integer maxPoolSize;

    @Schema(description = "超时时间(ms)")
    private Integer timeoutMs;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
