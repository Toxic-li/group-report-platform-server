package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.ValidatorDTO;
import com.groupreport.platform.service.ReportValidatorService;
import com.groupreport.platform.vo.ValidatorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 校验规则控制器
 */
@Slf4j
@RestController
@RequestMapping("/report-designer/validators")
@RequiredArgsConstructor
@Tag(name = "校验规则管理", description = "报表校验规则配置与执行接口")
public class ReportValidatorController {

    private final ReportValidatorService validatorService;

    @Operation(summary = "获取模板下的校验规则列表")
    @GetMapping("/template/{templateId}")
    public Result<List<ValidatorVO>> listValidators(@PathVariable Long templateId) {
        return Result.success(validatorService.getValidatorsByTemplateId(templateId));
    }

    @Operation(summary = "获取校验规则详情")
    @GetMapping("/{id}")
    public Result<ValidatorVO> getValidator(@PathVariable Long id) {
        return Result.success(validatorService.getValidatorDetail(id));
    }

    @Operation(summary = "创建校验规则")
    @PostMapping
    public Result<Long> createValidator(@Valid @RequestBody ValidatorDTO dto) {
        return Result.success(validatorService.createValidator(dto));
    }

    @Operation(summary = "更新校验规则")
    @PutMapping("/{id}")
    public Result<Void> updateValidator(@PathVariable Long id, @Valid @RequestBody ValidatorDTO dto) {
        dto.setId(id);
        validatorService.updateValidator(dto);
        return Result.success();
    }

    @Operation(summary = "删除校验规则")
    @DeleteMapping("/{id}")
    public Result<Void> deleteValidator(@PathVariable Long id) {
        validatorService.deleteValidator(id);
        return Result.success();
    }

    @Operation(summary = "执行数据校验", description = "传入单元格数据，返回校验错误Map")
    @PostMapping("/validate")
    public Result<Map<String, String>> validateData(
            @RequestParam Long templateId,
            @RequestBody Map<String, String> cellData,
            @RequestParam(defaultValue = "2") Integer trigger) {
        Map<String, String> errors = validatorService.validateData(templateId, cellData, trigger);
        return Result.success(errors);
    }
}
