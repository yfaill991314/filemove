package com.funi.filemove.dao;

import com.funi.filemove.po.FileMoveRecordPo;
import com.funi.filemove.po.MgDoorImgPo;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MgDoorImgPoMapper {
    int deleteByPrimaryKey(BigDecimal id);

    int insert(MgDoorImgPo record);

    int insertSelective(MgDoorImgPo record);

    MgDoorImgPo selectByPrimaryKey(BigDecimal id);

    int updateByPrimaryKeySelective(MgDoorImgPo record);

    int updateByPrimaryKeyWithBLOBs(MgDoorImgPo record);

    int updateByPrimaryKey(MgDoorImgPo record);

    List<MgDoorImgPo> selectListDoorImg(Map<String, Object> queryMap);

    List<MgDoorImgPo> selectListDoorImgByMinId(Map<String, Object> queryMap);

    List<MgDoorImgPo> selectMgDoorImgPoListByID(@Param("fileMoveRecordPoList") List<FileMoveRecordPo> fileMoveRecordPoList);
}