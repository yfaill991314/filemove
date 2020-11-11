package com.funi.filemove.dao;

import com.funi.filemove.po.CfDictPo;

import java.util.Map;

public interface CfDictPoMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(CfDictPo record);

    int insertSelective(CfDictPo record);

    CfDictPo selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(CfDictPo record);

    int updateByPrimaryKey(CfDictPo record);

    CfDictPo selectByMapParame(Map<String, Object> queryMap);
}