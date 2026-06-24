package com.groupreport.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.dto.DataSourceDTO;
import com.groupreport.platform.entity.SysDataSource;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.SysDataSourceMapper;
import com.groupreport.platform.service.DataSourceService;
import com.groupreport.platform.vo.DataSourceVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.DataSourceDef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSourceServiceImpl extends ServiceImpl<SysDataSourceMapper, SysDataSource>
        implements DataSourceService {

    private final ObjectMapper objectMapper;

    @Override
    public List<DataSourceVO> listAll() {
        List<SysDataSource> list = baseMapper.selectList(
                new LambdaQueryWrapper<SysDataSource>().eq(SysDataSource::getStatus, 1));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public DataSourceVO getDataSource(Long id) {
        SysDataSource ds = baseMapper.selectById(id);
        if (ds == null) throw new BusinessException("数据源不存在");
        return toVO(ds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDataSource(DataSourceDTO dto) {
        SysDataSource ds = convertToEntity(dto);
        baseMapper.insert(ds);
        log.info("创建数据源: id={}, code={}, type={}", ds.getId(), dto.getSourceCode(), dto.getSourceType());
        return ds.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataSource(DataSourceDTO dto) {
        if (dto.getId() == null) throw new BusinessException("数据源ID不能为空");
        SysDataSource existing = baseMapper.selectById(dto.getId());
        if (existing == null) throw new BusinessException("数据源不存在");
        SysDataSource updated = convertToEntity(dto);
        updated.setId(existing.getId());
        baseMapper.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataSource(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public boolean testConnection(Long sourceId) {
        SysDataSource ds = baseMapper.selectById(sourceId);
        if (ds == null) throw new BusinessException("数据源不存在");

        String type = ds.getSourceType();
        log.info("测试数据源连接: sourceId={}, type={}", sourceId, type);

        // 根据数据源类型进行连接测试
        // 预留：MySQL / PostgreSQL / API / ES / File 等
        return switch (type.toLowerCase()) {
            case "mysql", "postgresql" -> testJdbcConnection(ds);
            case "api" -> testApiConnection(ds);
            case "elasticsearch" -> testEsConnection(ds);
            case "file", "excel" -> true; // 文件类默认通过
            default -> {
                log.warn("不支持的数据源类型测试: {}", type);
                yield false;
            }
        };
    }

    @Override
    public Map<String, Object> executeQuery(Long sourceId, Map<String, Object> params) {
        SysDataSource ds = baseMapper.selectById(sourceId);
        if (ds == null) throw new BusinessException("数据源不存在");

        log.info("执行数据源查询: sourceId={}, type={}", sourceId, ds.getSourceType());

        // 预留核心扩展点：
        // MySQL → jdbcTemplate.queryForList(sql, params)
        // API  → restTemplate.exchange(url, params)
        // ES   → elasticsearchClient.search(query)
        // File → CSV/Excel parser

        Map<String, Object> result = new HashMap<>();
        result.put("sourceId", sourceId);
        result.put("sourceType", ds.getSourceType());
        result.put("status", "reserved");
        result.put("message", "查询引擎预留接口，待对接具体数据源驱动");
        return result;
    }

    @Override
    public DataSourceDef exportToDesignerFormat(Long sourceId) {
        SysDataSource ds = baseMapper.selectById(sourceId);
        if (ds == null) return null;

        DataSourceDef def = new DataSourceDef();
        def.setType(ds.getSourceType());
        def.setSourceId(String.valueOf(ds.getId()));
        def.setSourceName(ds.getSourceName());
        def.setQuery(ds.getQueryTemplate());
        def.setRefreshPolicy(ds.getRefreshPolicy());
        try { def.setFieldMapping(objectMapper.readValue(ds.getFieldMapping(), Map.class)); }
        catch (Exception e) { def.setFieldMapping(Map.of()); }
        return def;
    }

    // ==================== 私有方法 ====================

    private boolean testJdbcConnection(SysDataSource ds) {
        try {
            Map<String, Object> config = parseJson(ds.getConnectionConfig());
            String host = (String) config.getOrDefault("host", "localhost");
            int port = ((Number) config.getOrDefault("port", 3306)).intValue();
            log.info("JDBC连接测试: host={}, port={}", host, port);
            // TODO: 实际连接测试 - DriverManager.getConnection(...)
            return true;
        } catch (Exception e) {
            log.error("JDBC连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    private boolean testApiConnection(SysDataSource ds) {
        try {
            Map<String, Object> config = parseJson(ds.getConnectionConfig());
            String url = (String) config.getOrDefault("url", "");
            log.info("API连接测试: url={}", url);
            // TODO: 实际HTTP请求测试 - restTemplate.headForHeaders(url)
            return true;
        } catch (Exception e) {
            log.error("API连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    private boolean testEsConnection(SysDataSource ds) {
        try {
            Map<String, Object> config = parseJson(ds.getConnectionConfig());
            String hosts = (String) config.getOrDefault("hosts", "localhost:9200");
            log.info("ES连接测试: hosts={}", hosts);
            // TODO: 实际ES连接测试
            return true;
        } catch (Exception e) {
            log.error("ES连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, Object> parseJson(String json) {
        if (StrUtil.isBlank(json)) return Map.of();
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private SysDataSource convertToEntity(DataSourceDTO dto) {
        SysDataSource ds = new SysDataSource();
        ds.setSourceCode(dto.getSourceCode());
        ds.setSourceName(dto.getSourceName());
        ds.setSourceType(dto.getSourceType());
        try { ds.setConnectionConfig(objectMapper.writeValueAsString(dto.getConnectionConfig())); }
        catch (JsonProcessingException e) { ds.setConnectionConfig("{}"); }
        ds.setQueryTemplate(dto.getQueryTemplate());
        try { ds.setFieldMapping(objectMapper.writeValueAsString(dto.getFieldMapping())); }
        catch (JsonProcessingException e) { ds.setFieldMapping("{}"); }
        ds.setRefreshPolicy(dto.getRefreshPolicy());
        ds.setCronExpression(dto.getCronExpression());
        ds.setCacheTtl(dto.getCacheTtl());
        ds.setDescription(dto.getDescription());
        ds.setUsePool(dto.getUsePool());
        ds.setMaxPoolSize(dto.getMaxPoolSize());
        ds.setTimeoutMs(dto.getTimeoutMs());
        ds.setStatus(dto.getStatus());
        return ds;
    }

    private DataSourceVO toVO(SysDataSource ds) {
        DataSourceVO vo = new DataSourceVO();
        vo.setId(ds.getId());
        vo.setSourceCode(ds.getSourceCode());
        vo.setSourceName(ds.getSourceName());
        vo.setSourceType(ds.getSourceType());
        try { vo.setConnectionConfig(objectMapper.readValue(ds.getConnectionConfig(), Map.class)); }
        catch (Exception e) { vo.setConnectionConfig(Map.of()); }
        vo.setQueryTemplate(ds.getQueryTemplate());
        try { vo.setFieldMapping(objectMapper.readValue(ds.getFieldMapping(), Map.class)); }
        catch (Exception e) { vo.setFieldMapping(Map.of()); }
        vo.setRefreshPolicy(ds.getRefreshPolicy());
        vo.setCacheTtl(ds.getCacheTtl());
        vo.setDescription(ds.getDescription());
        vo.setUsePool(ds.getUsePool());
        vo.setMaxPoolSize(ds.getMaxPoolSize());
        vo.setTimeoutMs(ds.getTimeoutMs());
        vo.setStatus(ds.getStatus());
        vo.setCreatedAt(ds.getCreateTime());
        vo.setUpdatedAt(ds.getUpdateTime());
        return vo;
    }
}
