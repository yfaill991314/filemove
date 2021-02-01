package com.funi.filemove.dao;

import com.funi.filemove.po.ImgImagesPo;

public interface ImgImagesPoMapper {
    int insert(ImgImagesPo record);

    int insertSelective(ImgImagesPo record);
}