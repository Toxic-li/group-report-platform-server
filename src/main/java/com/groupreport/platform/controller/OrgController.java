package com.groupreport.platform.controller;

import com.groupreport.platform.common.Result;
import com.groupreport.platform.dto.OrgDTO;
import com.groupreport.platform.service.OrgService;
import com.groupreport.platform.vo.OrgVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织机构管理控制器
 */
@Tag(name = "组织机构管理", description = "组织机构的增删改查接口")
@RestController
@RequestMapping("/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @Operation(summary = "获取组织机构树")
    @GetMapping("/tree")
    public Result<List<OrgVO>> getOrgTree() {
        return Result.success(orgService.getOrgTree());
    }

    @Operation(summary = "获取组织详情")
    @GetMapping("/{id}")
    public Result<OrgVO> getOrgDetail(@PathVariable Long id) {
        return Result.success(orgService.getOrgDetail(id));
    }

    @Operation(summary = "创建组织机构")
    @PostMapping
    public Result<Long> createOrg(@Valid @RequestBody OrgDTO orgDTO) {
        return Result.success(orgService.createOrg(orgDTO));
    }

    @Operation(summary = "更新组织机构")
    @PutMapping("/{id}")
    public Result<Void> updateOrg(@PathVariable Long id, @Valid @RequestBody OrgDTO orgDTO) {
        orgDTO.setId(id);
        orgService.updateOrg(orgDTO);
        return Result.success();
    }

    @Operation(summary = "删除组织机构")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrg(@PathVariable Long id) {
        orgService.deleteOrg(id);
        return Result.success();
    }
}
