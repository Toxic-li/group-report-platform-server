package com.groupreport.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.groupreport.platform.entity.RptAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 */
@Mapper
public interface RptAuditLogMapper extends BaseMapper<RptAuditLog> {
}
