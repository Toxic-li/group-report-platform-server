package com.groupreport.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.groupreport.platform.dto.DataSourceDTO;
import com.groupreport.platform.entity.SysDataSource;
import com.groupreport.platform.vo.DataSourceVO;
import com.groupreport.platform.vo.ReportDesignerTemplateVO.DataSourceDef;

import java.util.List;
import java.util.Map;

/**
 * 数据源配置服务接口
 */
public interface DataSourceService extends IService<SysDataSource> {

    List<DataSourceVO> listAll();

    DataSourceVO getDataSource(Long id);

    Long createDataSource(DataSourceDTO dto);

    void updateDataSource(DataSourceDTO dto);

    void deleteDataSource(Long id);

    /**
     * 测试数据源连接是否可用
     */
    boolean testConnection(Long sourceId);

    /**
     * 通过数据源ID执行查询并返回结果集
     * 预留：后续对接 MySQL / API / ES 等多种数据源类型
     */
    Map<String, Object> executeQuery(Long sourceId, Map<String, Object> params);

    /**
     * 导出为设计器格式
     */
    DataSourceDef exportToDesignerFormat(Long sourceId);
}
