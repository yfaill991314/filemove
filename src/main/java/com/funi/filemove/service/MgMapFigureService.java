package com.funi.filemove.service;

import com.funi.filemove.po.MgMapFigurePo;

import java.util.Map;

/**
 * @ClassName MgMapFigureService
 * @Description
 * @Author YangFeng
 * @Date 2021/1/19 17:03
 * @Version 1.0
 */
public interface MgMapFigureService {
    Map<String, Object> MgMapFigureList(Map<String, Object> queryParams);

    MgMapFigurePo findMgMapFigureByQueryMap(Map<String, Object> queryParams) throws Exception;
}
