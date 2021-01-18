package com.funi.filemove.service;

import com.funi.filemove.po.CfFileDescPo;

import java.io.OutputStream;
import java.util.Map;

/**
 * @ClassName FileListService
 * @Description TODO
 * @Author YL
 * @Date 2020/6/28 16:16
 * @Version 1.0
 */
public interface FastFileService {
    Map<String, Object> findFileList(Map<String,Object> queryParams);

    CfFileDescPo findFileInfoByUuid(String fileUuid);

    int findDownLoad(String fileUuid, OutputStream os);
}
