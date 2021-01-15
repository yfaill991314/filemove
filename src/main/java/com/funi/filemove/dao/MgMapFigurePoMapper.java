package com.funi.filemove.dao;

import com.funi.filemove.po.MgMapFigurePo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MgMapFigurePoMapper {
    int deleteByPrimaryKey(BigDecimal id);

    int insert(MgMapFigurePo record);

    int insertSelective(MgMapFigurePo record);

    MgMapFigurePo selectByPrimaryKey(BigDecimal id);

    int updateByPrimaryKeySelective(MgMapFigurePo record);

    int updateByPrimaryKeyWithBLOBs(MgMapFigurePo record);

    int updateByPrimaryKey(MgMapFigurePo record);

    MgMapFigurePo selectFileByFileQuery(Map<String, Object> queryParams);

    int selectUnfinishCount();

    List<MgMapFigurePo> selectListFigure(Map<String, Object> queryMap);
}