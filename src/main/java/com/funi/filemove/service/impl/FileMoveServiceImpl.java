package com.funi.filemove.service.impl;

import com.alibaba.druid.sql.visitor.functions.If;
import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.Constants;
import com.funi.filemove.dao.*;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.movethread.MyThreadFacthory;
import com.funi.filemove.po.*;
import com.funi.filemove.service.FileMoveContext;
import com.funi.filemove.service.FileMoveService;
import com.funi.filemove.utils.MyUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.annotation.Resource;
import javax.transaction.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @ClassName FileMoveServiceImpl
 * @Description 文件迁移
 * @Author Feng.Yang
 * @Date 2020/10/29 14:33
 * @Version 1.0
 */
@Service
public class FileMoveServiceImpl implements FileMoveService {
    @Resource
    private CfFileDescPoMapper cfFileDescPoMapper;
    @Resource
    private FastDfsFileUpload fastDfsFileUpload;
    @Resource
    private MgMapFigurePoMapper mgMapFigurePoMapper;
    @Resource
    private MgDoorImgPoMapper mgDoorImgPoMapper;
    @Resource
    private ImgImagesPoMapper imgImagesPoMapper;
    @Resource
    private MgMapResultPoMapper mgMapResultPoMapper;
    @Resource
    private CfDictPoMapper cfDictPoMapper;
    //    @Resource
//    private JtaTransactionManager jtaTransactionManager;
    //    @Resource
//    private UserTransaction userTransaction;
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;
    @Resource
    private FileMoveContext fileMoveContext;


    private static volatile boolean inMoveTime = false;
    private static ExecutorService executorService = null;

    /**
     * @return void
     * @Author YangFeng
     * @Description //mgmapfigure表文件迁移
     * @Date 17:16 2021/1/21
     * @Param [fileMoveCurrentContext]
     **/
    public void moveMgMapFigureTable() {
        String theadId = Thread.currentThread().getName();
        String fileExe = "";
        String fileTypeName = "";
        String storeId = null;
        MgMapResultPo mgMapResultPo = null;
        MgMapFigurePo mgMapFigurePo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;
        //获取迁移上下文
        FileMoveCurrentContext fileMoveCurrentContext=fileMoveContext.getCurrentFileMoveContextInfo(Constants.MG_MAP_FIGURE);
        if (fileMoveCurrentContext == null) {
            System.out.println(Constants.MG_MAP_FIGURE+"表--未找到迁移上下文信息，迁移完成。");
            return;
        }

        while (FileMoveServiceImpl.inMoveTime) {
            try {
                //设置中间库主数据源
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                Map<String, Object> queryParams = new HashMap<>();
                //减1才能查询到 模为0的数据
//                queryParams.put("theadId", Integer.valueOf(theadId) - 1);
//                queryParams.put("theadSum", Constants.CPU_CORE_SIZE_IO);
                queryParams.put("dataSource", fileMoveCurrentContext.getCurMoveDataSource());
                queryParams.put("tableName", fileMoveCurrentContext.getCurMovetableName());
                queryParams.put("MoveStatus", fileMoveCurrentContext.getMoveStatus());
                fileMoveRecordPo = fileMoveRecordPoMapper.selectMoveTaskRecordByQueryParams(queryParams);
                if (fileMoveRecordPo == null) {
                    System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName() + "数据源---" + fileMoveCurrentContext.getCurMovetableName() + "表---" + "线程:" + theadId + ":数据已迁移完毕！！！");
                    return;
                }
//              默认当前迁移任务失败，迁移完成后修改为成功， 保证不再查询到该数据，防止异常数据阻塞迁移线程
                fileMoveRecordPo.setCreatetime(new Date());
                fileMoveRecordPo.setThreadId(theadId);
                fileMoveRecordPo.setMoveStatus("迁移失败");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getCurMoveDataSourceName());
                mgMapFigurePo = mgMapFigurePoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                if (mgMapFigurePo == null) {
                    fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }
                if (mgMapFigurePo.getResultsid() == null) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--Resultsid字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (mgMapFigurePo.getImage() == null || mgMapFigurePo.getImage().length == 0) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--Image字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (mgMapFigurePo.getImagename() == null) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--Imagename文件名称为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }
                if (mgMapFigurePo.getImgstyle() == null) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--Imgstyle文件后缀为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                //根据mgMapFigurePo 查询对应的成果
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getCurMoveDataSourceName());
                mgMapResultPo = mgMapResultPoMapper.selectByPrimaryKey(mgMapFigurePo.getResultsid());
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                //判断是否有对应的 成果业务件 没有对应成果 件放弃迁移
                if (mgMapResultPo == null) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--Resultsid字段未查找到对应成果");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }
                if (mgMapResultPo.getUuid() == null) {
                    fileMoveRecordPo.setRemark("对应成果无uuid无法关联");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                fileExe = mgMapFigurePo.getImgstyle();
                if (fileExe != null) {
                    int lc = fileExe.lastIndexOf(".");
                    if (lc >= 0) {
                        fileExe = fileExe.substring(lc + 1);
                    }
                }

                for (int retry = 1; retry <= Constants.MAX_RETRY_TIMES; retry++) {
                    try {
                        storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(mgMapFigurePo.getImage()), fileExe);
                        if (storeId != null) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (storeId == null) {
                    FastUpLoadContinuousFailureCount++;
                    fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(请及时确认)");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    System.out.println("迁移失败:上重试3次上传失败,storeId为空。(请及时确认)");
                    if (FastUpLoadContinuousFailureCount >= 5) {
                        fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败)");
                        fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                        System.out.println("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败);线程" + theadId + "已经终结");
                        break;
                    } else {
                        continue;
                    }
                } else {
                    FastUpLoadContinuousFailureCount = 0;
                }

                // 建立测绘成果与文件的 关联关系
                CfFileDescPo cfFileDescPo = new CfFileDescPo();
                cfFileDescPo.setUuid(MyUtils.getUuid36());
                cfFileDescPo.setFileStoreId(Constants.FAST_DFS_PREFIX + storeId);
                cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                cfFileDescPo.setFileSize(new BigDecimal(mgMapFigurePo.getImage().length));
                cfFileDescPo.setFileName(mgMapFigurePo.getImagename());
                cfFileDescPo.setExtName(fileExe);
                cfFileDescPo.setIsUse((short) 1);
                cfFileDescPo.setSystemCode(Constants.SYSTEM_CODE);
                cfFileDescPo.setBusinessTable(Constants.MG_MAP_FIGURE);
                cfFileDescPo.setBusinessUuid(mgMapResultPo.getUuid());
                fileTypeName = mgMapFigurePo.getImagetype();
                if ("地块及楼栋图".equals(fileTypeName)) {
                    fileTypeName = "楼栋及地块图";
                }
                cfFileDescPo.setBusinessName(fileTypeName);
                if (fileTypeName != null && !"".equals(fileTypeName.trim())) {
                    Map<String, Object> queryMap = new HashMap<>();
                    queryMap.put("fileTypeName", mgMapFigurePo.getImagetype());
                    ContextSynchronizationManager.bindResource("datasource", "cd");
                    CfDictPo cfDictPo = cfDictPoMapper.selectByMapParame(queryMap);
                    ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                    if (cfDictPo != null) {
                        cfFileDescPo.setBusinessType(cfDictPo.getUuid());
                    }
                }

                BigDecimal mgstatus = mgMapFigurePo.getMgstatus();
                if (mgstatus==null){
                    cfFileDescPo.setStatus((short) 1);
                }else {
                    if (mgstatus.compareTo(new BigDecimal(10)) == -1&&mgstatus.compareTo(new BigDecimal(-10)) == 1){
                        cfFileDescPo.setStatus(mgstatus.shortValueExact());
                    }else if (mgstatus.compareTo(new BigDecimal(-999)) == 0){
                        cfFileDescPo.setStatus((short) -9);
                    }else {
                        cfFileDescPo.setStatus((short) 1);
                    }
                }
                cfFileDescPo.setCreateTime(mgMapFigurePo.getRegidate());
                cfFileDescPo.setCreatorId(mgMapFigurePo.getCreater());
                cfFileDescPoMapper.insertSelective(cfFileDescPo);

                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
                fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
                fileMoveRecordPo.setMoveStatus("迁移成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName()+"--"+Constants.MG_MAP_FIGURE+"表--迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * @return void
     * @Author YangFeng
     * @Description //MgDoorImg表文件迁移
     * @Date 17:23 2021/1/21
     * @Param [fileMoveCurrentContext]
     **/
    public void moveMgDoorImgTable() {
        String theadId = Thread.currentThread().getName();
        String fileExe;
        String storeId = null;
        MgDoorImgPo mgDoorImgPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;
        //获取迁移上下文
        FileMoveCurrentContext fileMoveCurrentContext=fileMoveContext.getCurrentFileMoveContextInfo(Constants.MG_DOOR_IMG);
        if (fileMoveCurrentContext == null) {
            System.out.println(Constants.MG_DOOR_IMG+"表--未找到迁移上下文信息，迁移完成。");
            return;
        }

        while (FileMoveServiceImpl.inMoveTime) {
            try {
                //设置中间库主数据源
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                Map<String, Object> queryParams = new HashMap<>();
                //减1才能查询到 模为0的数据
//                queryParams.put("theadId", Integer.valueOf(theadId) - 1);
//                queryParams.put("theadSum", Constants.CPU_CORE_SIZE_IO);
                queryParams.put("dataSource", fileMoveCurrentContext.getCurMoveDataSource());
                queryParams.put("tableName", fileMoveCurrentContext.getCurMovetableName());
                queryParams.put("MoveStatus", fileMoveCurrentContext.getMoveStatus());
                fileMoveRecordPo = fileMoveRecordPoMapper.selectMoveTaskRecordByQueryParams(queryParams);
                if (fileMoveRecordPo == null) {
                    System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName() + "数据源---" + fileMoveCurrentContext.getCurMovetableName() + "表---" + "线程:" + theadId + ":数据已迁移完毕！！！");
                    return;
                }

//              默认当前迁移任务失败，迁移完成后修改为成功， 保证不再查询到该数据，防止异常数据阻塞迁移线程
                fileMoveRecordPo.setCreatetime(new Date());
                fileMoveRecordPo.setThreadId(theadId);
                fileMoveRecordPo.setMoveStatus("迁移失败");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getCurMoveDataSourceName());
                mgDoorImgPo = mgDoorImgPoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                if (mgDoorImgPo == null) {
                    fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (mgDoorImgPo.getImage() == null || mgDoorImgPo.getImage().length == 0) {
                    fileMoveRecordPo.setRemark("mgDoorImgPo--Image字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                fileExe=MyUtils.getFileExe(mgDoorImgPo.getImgstyle(),mgDoorImgPo.getProperty());

                if (mgDoorImgPo.getImgfilename() == null) {
                    mgDoorImgPo.setImgfilename(mgDoorImgPo.getId()+"."+fileExe);
                }

                for (int retry = 1; retry <= Constants.MAX_RETRY_TIMES; retry++) {
                    try {
                        storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(mgDoorImgPo.getImage()), fileExe);
                        if (storeId != null) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (storeId == null) {
                    FastUpLoadContinuousFailureCount++;
                    fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(请及时确认)");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    System.out.println("迁移失败:上重试3次上传失败,storeId为空。(请及时确认)");
                    if (FastUpLoadContinuousFailureCount >= 5) {
                        fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败)");
                        fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                        System.out.println("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败);线程" + theadId + "已经终结");
                        break;
                    } else {
                        continue;
                    }
                } else {
                    FastUpLoadContinuousFailureCount = 0;
                }

                // 建立测绘成果与文件的 关联关系
                CfFileDescPo cfFileDescPo = new CfFileDescPo();
                cfFileDescPo.setUuid(MyUtils.getUuid36());
                cfFileDescPo.setFileStoreId(Constants.FAST_DFS_PREFIX + storeId);
                cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                cfFileDescPo.setFileSize(new BigDecimal(mgDoorImgPo.getImage().length));
                cfFileDescPo.setFileName(mgDoorImgPo.getImgfilename());
                cfFileDescPo.setExtName(fileExe);
                cfFileDescPo.setIsUse((short) 1);
                cfFileDescPo.setSystemCode(Constants.SYSTEM_CODE);
                cfFileDescPo.setBusinessTable(Constants.MG_DOOR_IMG);
                cfFileDescPo.setBusinessUuid(mgDoorImgPo.getUuid());
                cfFileDescPo.setBusinessName("分层分户图");
                BigDecimal status = mgDoorImgPo.getStatus();
                if (status==null){
                    cfFileDescPo.setStatus((short) 1);
                }else {
                    if (status.compareTo(new BigDecimal(10)) == -1&&status.compareTo(new BigDecimal(-10)) == 1){
                        cfFileDescPo.setStatus(status.shortValueExact());
                    }else if (status.compareTo(new BigDecimal(-999)) == 0){
                        cfFileDescPo.setStatus((short) -9);
                    }else {
                        cfFileDescPo.setStatus((short) 1);
                    }
                }
                cfFileDescPo.setCreateTime(mgDoorImgPo.getRegidate());
                cfFileDescPo.setCreatorId(mgDoorImgPo.getCreater());
                cfFileDescPoMapper.insertSelective(cfFileDescPo);

                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
                fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
                fileMoveRecordPo.setMoveStatus("迁移成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName()+"--"+Constants.MG_DOOR_IMG+"表--迁移件：" + mgDoorImgPo.getId() + "迁移完成;文件storeId" + storeId);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * @return void
     * @Author YangFeng
     * @Description //Imgimages表文件迁移
     * @Date 17:23 2021/1/21
     * @Param [fileMoveCurrentContext]
     **/
    public void moveImgimagesTable() {
        String theadId = Thread.currentThread().getName();
        String fileExe;
        String storeId = null;
        ImgImagesPo imgImagesPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;
        //获取迁移上下文
        FileMoveCurrentContext fileMoveCurrentContext=fileMoveContext.getCurrentFileMoveContextInfo(Constants.IMG_IMAGES);
        if (fileMoveCurrentContext == null) {
            System.out.println(Constants.IMG_IMAGES+"表--未找到迁移上下文信息，迁移完成。");
            return;
        }

        while (FileMoveServiceImpl.inMoveTime) {
            try {
                //设置中间库主数据源
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                Map<String, Object> queryParams = new HashMap<>();
                //减1才能查询到 模为0的数据
//                queryParams.put("theadId", Integer.valueOf(theadId) - 1);
//                queryParams.put("theadSum", Constants.CPU_CORE_SIZE_IO);
                queryParams.put("dataSource", fileMoveCurrentContext.getCurMoveDataSource());
                queryParams.put("tableName", fileMoveCurrentContext.getCurMovetableName());
                queryParams.put("MoveStatus", fileMoveCurrentContext.getMoveStatus());
                fileMoveRecordPo = fileMoveRecordPoMapper.selectMoveTaskRecordByQueryParams(queryParams);
                if (fileMoveRecordPo == null) {
                    System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName() + "数据源---" + fileMoveCurrentContext.getCurMovetableName() + "表---" + "线程:" + theadId + ":数据已迁移完毕！！！");
                    return;
                }

//              默认当前迁移任务失败，迁移完成后修改为成功， 保证不再查询到该数据，防止异常数据阻塞迁移线程
                fileMoveRecordPo.setCreatetime(new Date());
                fileMoveRecordPo.setThreadId(theadId);
                fileMoveRecordPo.setMoveStatus("迁移失败");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getCurMoveDataSourceName());
                imgImagesPo = imgImagesPoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());

                if (imgImagesPo == null) {
                    fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (imgImagesPo.getImage() == null || imgImagesPo.getImage().length == 0) {
                    fileMoveRecordPo.setRemark("imgImages--Image字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                fileExe=MyUtils.getFileExe(imgImagesPo.getImgstyle(),imgImagesPo.getProperty());

                for (int retry = 1; retry <= Constants.MAX_RETRY_TIMES; retry++) {
                    try {
                        storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(imgImagesPo.getImage()), fileExe);
                        if (storeId != null) {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (storeId == null) {
                    FastUpLoadContinuousFailureCount++;
                    fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(请及时确认)");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    System.out.println("迁移失败:上重试3次上传失败,storeId为空。(请及时确认)");
                    if (FastUpLoadContinuousFailureCount >= 5) {
                        fileMoveRecordPo.setRemark("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败)");
                        fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                        System.out.println("重试3次上传失败,storeId为空。(文件服务器出现连续性上传失败);线程" + theadId + "已经终结");
                        break;
                    } else {
                        continue;
                    }
                } else {
                    FastUpLoadContinuousFailureCount = 0;
                }

                // 建立测绘成果与文件的 关联关系
                CfFileDescPo cfFileDescPo = new CfFileDescPo();
                cfFileDescPo.setUuid(MyUtils.getUuid36());
                cfFileDescPo.setFileStoreId(Constants.FAST_DFS_PREFIX + storeId);
                cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                cfFileDescPo.setFileSize(new BigDecimal(imgImagesPo.getImage().length));
                cfFileDescPo.setFileName(imgImagesPo.getId().toString()+'.'+fileExe);
                cfFileDescPo.setExtName(fileExe);
                cfFileDescPo.setIsUse((short) 1);
                cfFileDescPo.setSystemCode(Constants.SYSTEM_CODE);
                cfFileDescPo.setBusinessTable(Constants.IMG_IMAGES);
                cfFileDescPo.setBusinessUuid(imgImagesPo.getUuid());
                cfFileDescPo.setBusinessName("分层分户图");
                cfFileDescPo.setStatus((short) 1);
                cfFileDescPo.setCreateTime(imgImagesPo.getRegidate());
                cfFileDescPo.setCreatorId("程序迁入");
                cfFileDescPoMapper.insertSelective(cfFileDescPo);

                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
                fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
                fileMoveRecordPo.setMoveStatus("迁移成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println(fileMoveCurrentContext.getCurMoveDataSourceName()+"--"+Constants.IMG_IMAGES+"表--迁移件：" + imgImagesPo.getId() + "迁移完成;文件storeId" + storeId);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * @return boolean
     * @Author YangFeng
     * @Description //停止迁移
     * @Date 10:11 2021/1/15
     * @Param []
     **/
    @Override
    public boolean stopMove() {
        //立即停止
        if (executorService != null && !executorService.isShutdown()) {
            FileMoveServiceImpl.inMoveTime = false;
//            executorService.shutdown();
            executorService.shutdownNow();
            executorService = null;
        }
        return true;
    }

    /**
     * @return boolean
     * @Author YangFeng
     * @Description //开始迁移
     * @Date 10:11 2021/1/15
     * @Param []
     **/
    @Override
    public boolean startMove() {
        MyThreadFacthory threadFactory = new MyThreadFacthory();
        executorService = new ThreadPoolExecutor(Constants.CPU_CORE_SIZE_IO, Constants.CPU_CORE_SIZE_IO,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

        FileMoveServiceImpl.inMoveTime = true;
        executorService.submit(this::moveMgMapFigureTable);
        executorService.submit(this::moveMgDoorImgTable);
        executorService.submit(this::moveImgimagesTable);

        return true;
    }

}
