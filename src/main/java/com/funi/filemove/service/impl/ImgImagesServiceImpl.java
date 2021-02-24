package com.funi.filemove.service.impl;

import com.funi.filemove.dao.ImgImagesPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.ImgImagesPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.service.ImgImagesService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName ImgImagesServiceImpl
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/2/22 15:22
 * @Version 1.0
 */
@Service
public class ImgImagesServiceImpl implements ImgImagesService {

    @Resource
    private ImgImagesPoMapper imgImagesPoMapper;

    @Override
    public Map<String, Object> ImgImagesList(Map<String, Object> queryParams) {
        int pageNum = Integer.parseInt(queryParams.get("page").toString());
        int pageSize= Integer.parseInt(queryParams.get("limit").toString());
        PageHelper.startPage(pageNum, pageSize);
        if (queryParams.get("dataSource")==null || "".equals(queryParams.get("dataSource")) ){
            queryParams.put("dataSource","cd");
        }
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));

        List<ImgImagesPo> imgImagesPoList = imgImagesPoMapper.selectListImgImages(queryParams);
        for (ImgImagesPo imgImagesPo:imgImagesPoList) {
            imgImagesPo.setDataSource(queryParams.get("dataSource").toString());
        }

        ContextSynchronizationManager.bindResource("datasource",null);
        PageInfo<ImgImagesPo> pageInfo = new PageInfo<>(imgImagesPoList);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", imgImagesPoList);
        result.put("total", total);
        return result;
    }

    @Override
    public ImgImagesPo findImgImagesByQueryMap(Map<String, Object> queryParams) {
        ContextSynchronizationManager.bindResource("datasource",queryParams.get("dataSource"));
        ImgImagesPo imgImagesPo = imgImagesPoMapper.selectByPrimaryKey(new BigDecimal(queryParams.get("id").toString()));
        ContextSynchronizationManager.bindResource("datasource",null);
        return imgImagesPo;
    }
}
