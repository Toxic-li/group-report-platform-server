package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.DataSourceDTO;
import com.groupreport.platform.service.DataSourceService;
import com.groupreport.platform.vo.DataSourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/report-designer/data-sources")
@RequiredArgsConstructor
@Tag(name = "数据源管理", description = "数据源配置与查询接口")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @Operation(summary = "获取所有数据源列表")
    @GetMapping
    public Result<List<DataSourceVO>> listDataSources() {
        return Result.success(dataSourceService.listAll());
    }

    @Operation(summary = "获取数据源详情")
    @GetMapping("/{id}")
    public Result<DataSourceVO> getDataSource(@PathVariable Long id) {
        return Result.success(dataSourceService.getDataSource(id));
    }

    @Operation(summary = "创建数据源")
    @PostMapping
    public Result<Long> createDataSource(@Valid @RequestBody DataSourceDTO dto) {
        return Result.success(dataSourceService.createDataSource(dto));
    }

    @Operation(summary = "更新数据源")
    @PutMapping("/{id}")
    public Result<Void> updateDataSource(@PathVariable Long id, @Valid @RequestBody DataSourceDTO dto) {
        dto.setId(id);
        dataSourceService.updateDataSource(dto);
        return Result.success();
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return Result.success();
    }

    @Operation(summary = "测试数据源连接")
    @PostMapping("/{id}/test-connection")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        boolean ok = dataSourceService.testConnection(id);
        return Result.success(ok);
    }

    @Operation(summary = "执行数据源查询（预留）")
    @PostMapping("/{id}/query")
    public Result<Map<String, Object>> executeQuery(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> params) {
        Map<String, Object> result = dataSourceService.executeQuery(id, params != null ? params : Map.of());
        return Result.success(result);
    }
}
