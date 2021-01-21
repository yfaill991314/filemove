package com.funi.filemove.service.impl;

import com.funi.filemove.dao.MgMapFigurePoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.FileMoveRecordPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.service.MgMapFigureService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MgMapFigureServiceImpl
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/1/19 17:04
 * @Version 1.0
 */
@Service
public class MgMapFigureServiceImpl implements MgMapFigureService {
    @Resource
    private MgMapFigurePoMapper mgMapFigurePoMapper;

    @Override
    public Map<String, Object> MgMapFigureList(Map<String, Object> queryParams) {
        int pageNum = Integer.parseInt(queryParams.get("page").toString());
        int pageSize= Integer.parseInt(queryParams.get("limit").toString());
        PageHelper.startPage(pageNum, pageSize);
        if (queryParams.get("dataSource")==null || "".equals(queryParams.get("dataSource")) ){
            queryParams.put("dataSource","cd");
        }
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));

        List<MgMapFigurePo> mgMapFigurePos = mgMapFigurePoMapper.selectListFigure(queryParams);
        for (MgMapFigurePo mgMapFigurePo:mgMapFigurePos) {
            mgMapFigurePo.setDataSource(queryParams.get("dataSource").toString());
        }

        ContextSynchronizationManager.bindResource("datasource",null);
        PageInfo<MgMapFigurePo> pageInfo = new PageInfo<>(mgMapFigurePos);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", mgMapFigurePos);
        result.put("total", total);
        return result;
    }

    @Override
    public MgMapFigurePo findMgMapFigureByQueryMap(Map<String, Object> queryParams) throws Exception {
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));
        MgMapFigurePo mgMapFigurePo = mgMapFigurePoMapper.selectByPrimaryKey(new BigDecimal(queryParams.get("id").toString()));
        ContextSynchronizationManager.bindResource("datasource",null);
        return mgMapFigurePo;
    }
}
