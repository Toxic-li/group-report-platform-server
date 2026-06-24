package com.groupreport.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.groupreport.platform.common.Constants;
import com.groupreport.platform.common.ResultCode;
import com.groupreport.platform.dto.OrgDTO;
import com.groupreport.platform.entity.SysOrg;
import com.groupreport.platform.exception.BusinessException;
import com.groupreport.platform.mapper.SysOrgMapper;
import com.groupreport.platform.service.OrgService;
import com.groupreport.platform.vo.OrgVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 组织机构服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements OrgService {

    @Override
    public List<OrgVO> getOrgTree() {
        // 查询所有启用的组织
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getStatus, Constants.Status.ENABLED)
               .orderByAsc(SysOrg::getSortOrder)
               .orderByAsc(SysOrg::getId);
        List<SysOrg> allOrgs = baseMapper.selectList(wrapper);

        // 转换为VO
        List<OrgVO> voList = allOrgs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList, 0L);
    }

    @Override
    public List<Long> getOrgAndChildrenIds(Long orgId) {
        List<Long> ids = new ArrayList<>();
        ids.add(orgId);

        // 查询所有子节点
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SysOrg::getTreePath, "," + orgId + ",")
               .or()
               .eq(SysOrg::getId, orgId);
        List<SysOrg> children = baseMapper.selectList(wrapper);

        children.forEach(org -> ids.add(org.getId()));
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrg(OrgDTO orgDTO) {
        // 检查编码是否已存在
        LambdaQueryWrapper<SysOrg> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SysOrg::getOrgCode, orgDTO.getOrgCode());
        if (baseMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(ResultCode.ORG_CODE_EXISTS);
        }

        SysOrg org = new SysOrg();
        BeanUtils.copyProperties(orgDTO, org);

        // 处理层级和路径
        if (org.getParentId() == null || org.getParentId() == 0) {
            org.setParentId(0L);
            org.setLevel(1);
            org.setTreePath(",0,");
        } else {
            SysOrg parent = baseMapper.selectById(org.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.ORG_NOT_FOUND);
            }
            org.setLevel(parent.getLevel() + 1);
            org.setTreePath(parent.getTreePath() + parent.getId() + ",");
        }

        baseMapper.insert(org);
        log.info("创建组织成功: {}", org.getOrgName());
        return org.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrg(OrgDTO orgDTO) {
        SysOrg org = baseMapper.selectById(orgDTO.getId());
        if (org == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }

        // 检查编码是否被其他组织使用
        if (!org.getOrgCode().equals(orgDTO.getOrgCode())) {
            LambdaQueryWrapper<SysOrg> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(SysOrg::getOrgCode, orgDTO.getOrgCode())
                       .ne(SysOrg::getId, orgDTO.getId());
            if (baseMapper.selectCount(checkWrapper) > 0) {
                throw new BusinessException(ResultCode.ORG_CODE_EXISTS);
            }
        }

        BeanUtils.copyProperties(orgDTO, org, "id", "level", "treePath");
        baseMapper.updateById(org);
        log.info("更新组织成功: {}", org.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrg(Long orgId) {
        // 检查是否有子节点
        LambdaQueryWrapper<SysOrg> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(SysOrg::getParentId, orgId);
        Long childCount = baseMapper.selectCount(childWrapper);
        if (childCount > 0) {
            throw new BusinessException(ResultCode.ORG_HAS_CHILDREN);
        }

        baseMapper.deleteById(orgId);
        log.info("删除组织成功: {}", orgId);
    }

    @Override
    public OrgVO getOrgDetail(Long orgId) {
        SysOrg org = baseMapper.selectById(orgId);
        if (org == null) {
            throw new BusinessException(ResultCode.ORG_NOT_FOUND);
        }
        return convertToVO(org);
    }

    /**
     * 构建树形结构
     */
    private List<OrgVO> buildTree(List<OrgVO> allNodes, Long parentId) {
        List<OrgVO> tree = new ArrayList<>();

        for (OrgVO node : allNodes) {
            if ((parentId == 0 && node.getParentId() == null) || 
                (node.getParentId() != null && node.getParentId().equals(parentId))) {
                List<OrgVO> children = buildTree(allNodes, node.getId());
                node.setChildren(children.isEmpty() ? null : children);
                tree.add(node);
            }
        }

        return tree;
    }

    /**
     * 转换为VO
     */
    private OrgVO convertToVO(SysOrg org) {
        OrgVO vo = new OrgVO();
        BeanUtils.copyProperties(org, vo);

        // 设置组织类型名称
        switch (org.getOrgType()) {
            case Constants.OrgType.GROUP:
                vo.setOrgTypeName("集团");
                break;
            case Constants.OrgType.SUB_COMPANY:
                vo.setOrgTypeName("子公司");
                break;
            case Constants.OrgType.DEPARTMENT:
                vo.setOrgTypeName("部门");
                break;
            case Constants.OrgType.TEAM:
                vo.setOrgTypeName("小组");
                break;
            default:
                vo.setOrgTypeName("未知");
        }

        return vo;
    }
}
