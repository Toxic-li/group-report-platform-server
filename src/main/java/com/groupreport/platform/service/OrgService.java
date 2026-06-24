package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.OrgDTO;
import com.groupreport.platform.entity.SysOrg;
import com.groupreport.platform.vo.OrgVO;

import java.util.List;

/**
 * 组织机构服务接口
 */
public interface OrgService extends IService<SysOrg> {

    /**
     * 获取组织机构树
     * @return 组织机构树形结构
     */
    List<OrgVO> getOrgTree();

    /**
     * 获取指定组织的所有子节点ID（包含自身）
     * @param orgId 组织ID
     * @return 组织ID列表
     */
    List<Long> getOrgAndChildrenIds(Long orgId);

    /**
     * 创建组织机构
     * @param orgDTO 组织信息
     * @return 组织ID
     */
    Long createOrg(OrgDTO orgDTO);

    /**
     * 更新组织机构
     * @param orgDTO 组织信息
     */
    void updateOrg(OrgDTO orgDTO);

    /**
     * 删除组织机构
     * @param orgId 组织ID
     */
    void deleteOrg(Long orgId);

    /**
     * 获取组织详情
     * @param orgId 组织ID
     * @return 组织信息
     */
    OrgVO getOrgDetail(Long orgId);
}
