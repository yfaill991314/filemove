package com.funi.filemove.dao;

import com.funi.filemove.po.FileMoveRecordPo;

import java.util.List;
import java.util.Map;

public interface FileMoveRecordPoMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(FileMoveRecordPo record);

    int insertSelective(FileMoveRecordPo record);

    FileMoveRecordPo selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(FileMoveRecordPo record);

    int updateByPrimaryKey(FileMoveRecordPo record);

    List<FileMoveRecordPo> selectListRecord(Map<String, Object> queryMap);
}