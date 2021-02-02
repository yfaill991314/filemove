package com.funi.filemove.service.impl;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.Constants;
import com.funi.filemove.dao.CfFileDescPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.CfFileDescPo;
import com.funi.filemove.service.FastFileService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName FileListServiceImpl
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/6/28 16:17
 * @Version 1.0
 */
@Service
public class FastFileServiceImpl implements FastFileService {
    @Resource
    private CfFileDescPoMapper cfFileDescPoMapper;
    @Resource
    private FastDfsFileUpload fastDfsFileUpload;

    @Override
    public Map<String, Object> findFileList(Map<String,Object> queryParams) {
        int pageNum = Integer.parseInt(queryParams.get("page").toString());
        int pageSize= Integer.parseInt(queryParams.get("limit").toString());
        PageHelper.startPage(pageNum, pageSize);
        ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
        List<CfFileDescPo> cfFileDescPos = cfFileDescPoMapper.selectFileListByFileQuery(queryParams);
        PageInfo<CfFileDescPo> pageInfo = new PageInfo<>(cfFileDescPos);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", cfFileDescPos);
        result.put("total", total);
        return result;
    }

    @Override
    public CfFileDescPo findFileInfoByUuid(String fileUuid) {
        ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
        return  cfFileDescPoMapper.selectByPrimaryKey(fileUuid);
    }

    @Override
    public int findDownLoad(String fileUuid, OutputStream os) {
        ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
        CfFileDescPo cfFileDescPO = cfFileDescPoMapper.selectByPrimaryKey(fileUuid);
        if (cfFileDescPO==null){
            return -1;
        }
        String fileStoreId = this.getFastDfsFileId(cfFileDescPO.getFileStoreId());
        return fastDfsFileUpload.fileDownload(fileStoreId,os);
    }

    private String getFastDfsFileId(String fileId) {
        return fileId.replace("fastdfs://", "");
    }
}
