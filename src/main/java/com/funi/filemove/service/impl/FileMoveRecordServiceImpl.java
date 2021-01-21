package com.funi.filemove.service.impl;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.Constants;
import com.funi.filemove.dao.*;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.*;
import com.funi.filemove.service.FileMoveRecordService;
import com.funi.filemove.utils.MyUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sun.org.apache.xpath.internal.objects.XString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class FileMoveRecordServiceImpl implements FileMoveRecordService {
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;
    @Resource
    private MgMapFigurePoMapper mgMapFigurePoMapper;
    @Resource
    private MgMapResultPoMapper mgMapResultPoMapper;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private CfDictPoMapper cfDictPoMapper;
    @Resource
    private FastDfsFileUpload fastDfsFileUpload;
    @Resource
    private CfFileDescPoMapper cfFileDescPoMapper;

    @Override
    public Map<String, Object> fileMoveRecordList(Map<String, Object> queryParams) {
        int pageNum = Integer.parseInt(queryParams.get("page").toString());
        int pageSize = Integer.parseInt(queryParams.get("limit").toString());
        PageHelper.startPage(pageNum, pageSize);

        if (queryParams.get("moveStatus") != null && !"".equals(queryParams.get("moveStatus"))) {
            List<String> moveStatusList = new ArrayList<>();
            moveStatusList.add(queryParams.get("moveStatus").toString());
            queryParams.put("moveStatusList", moveStatusList);
        }
        List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryParams);
        PageInfo<FileMoveRecordPo> pageInfo = new PageInfo<>(fileMoveRecordPos);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", fileMoveRecordPos);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> remigrate(Map<String, Object> queryParams) {
        String fileExe = "";
        String fileTypeName = "";
        String storeId = "";
        MgMapResultPo mgMapResultPo = null;
        MgMapFigurePo mgMapFigurePo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        String dataSorceName=MyUtils.getDataSouceNameByCode(queryParams.get("dataSource").toString());

        //设置中间库主数据源
        ContextSynchronizationManager.bindResource("datasource", "zj");
        try {
            fileMoveRecordPo = fileMoveRecordPoMapper.selectMoveTaskRecordByQueryParams(queryParams);
            if (fileMoveRecordPo == null) {
                throw new Exception("未找到相应任务");
            }

//              默认当前迁移任务失败，迁移完成后修改为成功， 保证不再查询到该数据，防止异常数据阻塞迁移线程
            fileMoveRecordPo.setCreatetime(new Date());
            fileMoveRecordPo.setThreadId("0");
            fileMoveRecordPo.setMoveStatus("迁移失败");
            fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

            ContextSynchronizationManager.bindResource("datasource",dataSorceName);
            mgMapFigurePo = mgMapFigurePoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
            ContextSynchronizationManager.bindResource("datasource", "zj");

            if (mgMapFigurePo == null) {
                fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }
            if (mgMapFigurePo.getResultsid() == null) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--Resultsid字段为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }
            if (mgMapFigurePo.getMgstatus() == null) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--mgstatus文件状态为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }
            if (mgMapFigurePo.getImage() == null || mgMapFigurePo.getImage().length == 0) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--Image字段为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }


            //根据mgMapFigurePo 查询对应的成果
            ContextSynchronizationManager.bindResource("datasource", dataSorceName);
            mgMapResultPo = mgMapResultPoMapper.selectByPrimaryKey(mgMapFigurePo.getResultsid());
            ContextSynchronizationManager.bindResource("datasource", "zj");
            //判断是否有对应的 成果业务件 没有对应成果 件放弃迁移
            if (mgMapResultPo == null) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--Resultsid字段未查找到对应成果");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }
            if (mgMapResultPo.getUuid() == null) {
                fileMoveRecordPo.setRemark("对应成果无uuid无法关联");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            fileExe = mgMapFigurePo.getImgstyle();
            if (fileExe != null) {
                int lc = fileExe.lastIndexOf(".");
                if (lc >= 0) {
                    fileExe = fileExe.substring(lc + 1);
                }
            }
            storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(mgMapFigurePo.getImage()), fileExe);
            if (storeId == null) {
                fileMoveRecordPo.setRemark("上传文件失败,storeId为空。(请确认文件服务器工作状态)");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("迁移失败:上传文件失败,storeId为空。(请确认文件服务器工作状态");
                return null;
            }

            // 建立测绘成果与文件的 关联关系
            CfFileDescPo cfFileDescPo = new CfFileDescPo();
            cfFileDescPo.setUuid(commonMapper.selectSystemUUid());
            cfFileDescPo.setFileStoreId(Constants.FAST_DFS_PREFIX + storeId);
            cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
            cfFileDescPo.setFileSize(new BigDecimal(mgMapFigurePo.getImage().length));
            cfFileDescPo.setFileName(mgMapFigurePo.getImagename());
            cfFileDescPo.setExtName(fileExe);
            cfFileDescPo.setIsUse((short) 1);
            cfFileDescPo.setSystemCode(Constants.SYSTEM_CODE);
            cfFileDescPo.setBusinessTable(null);
            cfFileDescPo.setBusinessUuid(mgMapResultPo.getUuid());
            fileTypeName = mgMapFigurePo.getImagetype();
            if ("地块及楼栋图".equals(fileTypeName)) {
                fileTypeName = "楼栋及地块图";
            }
            cfFileDescPo.setBusinessName(fileTypeName);
            if (fileTypeName != null && !"".equals(fileTypeName.trim())) {
                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("fileTypeName", mgMapFigurePo.getImagetype());
                CfDictPo cfDictPo = cfDictPoMapper.selectByMapParame(queryMap);
                if (cfDictPo != null) {
                    cfFileDescPo.setBusinessType(cfDictPo.getUuid());
                }
            }

            if ((new BigDecimal(-999)).equals(mgMapFigurePo.getMgstatus())) {
                cfFileDescPo.setStatus((short) -9);
            } else {
                cfFileDescPo.setStatus(mgMapFigurePo.getMgstatus().shortValueExact());
            }

            cfFileDescPo.setCreateTime(mgMapFigurePo.getRegidate());
            cfFileDescPo.setCreatorId(mgMapFigurePo.getCreater());
            cfFileDescPoMapper.insertSelective(cfFileDescPo);

            fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
            fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
            fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
            fileMoveRecordPo.setMoveStatus("迁移成功");
            fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
            System.out.println("成果号" + mgMapResultPo.getId() + "迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }
}
