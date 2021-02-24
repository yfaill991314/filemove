package com.funi.filemove.service.impl;

import com.funi.filemove.dao.MgDoorImgPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.MgDoorImgPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.service.MgDoorimgService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MgDoorimgServiceImpl
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/2/22 15:47
 * @Version 1.0
 */
@Service
public class MgDoorimgServiceImpl implements MgDoorimgService {
    @Resource
    private MgDoorImgPoMapper mgDoorImgPoMapper;

    @Override
    public Map<String, Object> MgDoorimgList(Map<String, Object> queryParams) {
        int pageNum = Integer.parseInt(queryParams.get("page").toString());
        int pageSize= Integer.parseInt(queryParams.get("limit").toString());
        PageHelper.startPage(pageNum, pageSize);
        if (queryParams.get("dataSource")==null || "".equals(queryParams.get("dataSource")) ){
            queryParams.put("dataSource","cd");
        }
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));

        List<MgDoorImgPo> mgDoorImgPoList = mgDoorImgPoMapper.selectListDoorImg(queryParams);
        for (MgDoorImgPo mgDoorImgPo:mgDoorImgPoList) {
            mgDoorImgPo.setDataSource(queryParams.get("dataSource").toString());
        }

        ContextSynchronizationManager.bindResource("datasource",null);
        PageInfo<MgDoorImgPo> pageInfo = new PageInfo<>(mgDoorImgPoList);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", mgDoorImgPoList);
        result.put("total", total);
        return result;
    }

    @Override
    public MgDoorImgPo findMgDoorimgByQueryMap(Map<String, Object> queryParams) {
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));
        MgDoorImgPo mgDoorImgPo = mgDoorImgPoMapper.selectByPrimaryKey(new BigDecimal(queryParams.get("id").toString()));
        ContextSynchronizationManager.bindResource("datasource",null);
        return mgDoorImgPo;
    }
}
