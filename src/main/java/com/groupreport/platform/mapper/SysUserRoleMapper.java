package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除角色关联
     */
    default void deleteByUserId(Long userId) {
        delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
    }
}
