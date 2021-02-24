package com.funi.filemove.service;

import com.funi.filemove.po.ImgImagesPo;

import java.util.Map;

/**
 * @ClassName ImgImagesService
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/2/22 15:20
 * @Version 1.0
 */
public interface ImgImagesService {
    Map<String, Object> ImgImagesList(Map<String, Object> queryParams);

    ImgImagesPo findImgImagesByQueryMap(Map<String, Object> queryParams);
}
