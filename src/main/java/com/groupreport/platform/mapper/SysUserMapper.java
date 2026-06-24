package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    default SysUser selectByUsername(String username) {
        return selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }
}
