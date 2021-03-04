package com.funi.filemove.dao;

import com.funi.filemove.po.CfFileDescPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CfFileDescPoMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(CfFileDescPo record);

    int insertSelective(CfFileDescPo record);

    CfFileDescPo selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(CfFileDescPo record);

    int updateByPrimaryKey(CfFileDescPo record);

    List<CfFileDescPo> selectFileListByFileQuery(Map<String, Object> queryParams);

    CfFileDescPo selectFileListByFigIdAndBusUuid(Map<String, Object> fileQueryParams);

    void insertAll(@Param("cfFileDescPoList") List<CfFileDescPo> cfFileDescPoList);

    List<CfFileDescPo> findFileListNotInRecord(Map<String, Object> queryMap);
}