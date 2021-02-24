package com.funi.filemove.dao;

import com.funi.filemove.po.ImgImagesPo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ImgImagesPoMapper {
    int insert(ImgImagesPo record);

    int insertSelective(ImgImagesPo record);

    List<ImgImagesPo> selectListImgImages(Map<String, Object> queryMap);

    ImgImagesPo selectByPrimaryKey(BigDecimal bigDecimal);

    List<ImgImagesPo> selectListImgImagesByMinId(Map<String, Object> queryMap);
}