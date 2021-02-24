package com.funi.filemove.service;

import com.funi.filemove.po.MgDoorImgPo;

import java.util.Map;

/**
 * @ClassName MgDoorimgService
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/2/22 15:46
 * @Version 1.0
 */
public interface MgDoorimgService {
    Map<String, Object> MgDoorimgList(Map<String, Object> queryParams);

    MgDoorImgPo findMgDoorimgByQueryMap(Map<String, Object> queryParams);
}
