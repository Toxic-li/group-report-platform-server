package com.groupreport.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源配置实体
 */
@Data
@TableName("sys_data_source")
public class SysDataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 数据源编码 */
    private String sourceCode;

    /** 数据源名称 */
    private String sourceName;

    /** 数据源类型：mysql/postgresql/api/elasticsearch/file/excel */
    private String sourceType;

    /** 连接配置JSON（主机、端口、用户名等） */
    private String connectionConfig;

    /** SQL查询语句或API路径 */
    private String queryTemplate;

    /** 字段映射配置JSON */
    private String fieldMapping;

    /** 刷新策略：manual/auto_5min/auto_1hour/daily/cron */
    private String refreshPolicy;

    /** Cron表达式（refreshPolicy=cron时使用） */
    private String cronExpression;

    /** 缓存过期时间（秒），0表示不缓存 */
    private Long cacheTtl;

    /** 描述 */
    private String description;

    /** 是否启用连接池 */
    private Boolean usePool;

    /** 最大连接数 */
    private Integer maxPoolSize;

    /** 超时时间（毫秒） */
    private Integer timeoutMs;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
