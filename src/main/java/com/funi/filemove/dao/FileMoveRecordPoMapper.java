package com.funi.filemove.dao;

import com.funi.filemove.po.FileMoveRecordPo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
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

    int queryMoveRecordCount(Map<String, Object> queryMap);

    FileMoveRecordPo selectMoveTaskRecordByQueryParams(Map<String, Object> queryParams);

    void insertAll(@Param("fileMoveRecordPoList") List<FileMoveRecordPo> fileMoveRecordPoList);

    BigDecimal selectMaxBizIdByParams(Map<String, Object> recordQueryMap);
}