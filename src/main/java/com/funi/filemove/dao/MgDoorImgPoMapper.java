package com.funi.filemove.dao;

import com.funi.filemove.po.MgDoorImgPo;
import java.math.BigDecimal;

public interface MgDoorImgPoMapper {
    int deleteByPrimaryKey(BigDecimal id);

    int insert(MgDoorImgPo record);

    int insertSelective(MgDoorImgPo record);

    MgDoorImgPo selectByPrimaryKey(BigDecimal id);

    int updateByPrimaryKeySelective(MgDoorImgPo record);

    int updateByPrimaryKeyWithBLOBs(MgDoorImgPo record);

    int updateByPrimaryKey(MgDoorImgPo record);
}