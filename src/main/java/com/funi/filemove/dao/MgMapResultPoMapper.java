package com.funi.filemove.dao;

import com.funi.filemove.po.MgMapResultPo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MgMapResultPoMapper {
    int deleteByPrimaryKey(BigDecimal id);

    int insert(MgMapResultPo record);

    int insertSelective(MgMapResultPo record);

    MgMapResultPo selectByPrimaryKey(BigDecimal id);

    int updateByPrimaryKeySelective(MgMapResultPo record);

    int updateByPrimaryKeyWithBLOBs(MgMapResultPo record);

    int updateByPrimaryKey(MgMapResultPo record);

    List<MgMapResultPo> selectMgResultListByFileQuery(Map<String, Object> queryParams);
}