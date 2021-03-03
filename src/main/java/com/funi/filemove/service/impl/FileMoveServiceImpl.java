package com.funi.filemove.service.impl;

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
    private CommonMapper commonMapper;
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
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * @Author YangFeng
     * @Description //开始迁移
     * @Date 17:17 2021/1/21
     * @Param []
     * @return void
     **/
    @Override
    public void fileMove() {
        //修改本次迁移的 迁移上下文信息
        fileMoveContext.setCurrentFileMoveContextInfo();
        final FileMoveCurrentContext fileMoveCurrentContext = FileMoveContextImpl.getFileMoveCurrentContext();
        if (fileMoveCurrentContext == null) {
            System.out.println("未找到迁移上下文信息，迁移完成。");
            return;
        }

        if (Constants.MG_MAP_FIGURE.equals(fileMoveCurrentContext.getCurMovetableName())) {
            this.moveMgMapFigureTable(fileMoveCurrentContext);
        } else if (Constants.MG_DOOR_IMG.equals(fileMoveCurrentContext.getCurMovetableName())) {
            this.moveMgDoorImgTable(fileMoveCurrentContext);
        } else if (Constants.IMG_IMAGES.equals(fileMoveCurrentContext.getCurMovetableName())) {
            this.moveImgimagesTable(fileMoveCurrentContext);
        }

    }

    /**
     * @return void
     * @Author YangFeng
     * @Description //mgmapfigure表文件迁移
     * @Date 17:16 2021/1/21
     * @Param [fileMoveCurrentContext]
     **/
    public void moveMgMapFigureTable(FileMoveCurrentContext fileMoveCurrentContext) {
        String theadId = Thread.currentThread().getName();
        String fileExe = "";
        String fileTypeName = "";
        String storeId = null;
        MgMapResultPo mgMapResultPo = null;
        MgMapFigurePo mgMapFigurePo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;

        long startTime=0;
        long endTime=0;

        while (FileMoveServiceImpl.inMoveTime) {
            try {

                startTime = System.currentTimeMillis();

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

                endTime = System.currentTimeMillis();
                System.out.println("查询任务段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

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
                if (mgMapFigurePo.getMgstatus() == null) {
                    fileMoveRecordPo.setRemark("mgMapFigurePo--mgstatus文件状态为空");
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

                endTime = System.currentTimeMillis();
                System.out.println("读取大字段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

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

                endTime = System.currentTimeMillis();
                System.out.println("查询成果段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

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

                endTime = System.currentTimeMillis();
                System.out.println("文件上传段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

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

                cfFileDescPo.setStatus(mgMapFigurePo.getMgstatus().shortValueExact());
                cfFileDescPo.setCreateTime(mgMapFigurePo.getRegidate());
                cfFileDescPo.setCreatorId(mgMapFigurePo.getCreater());
                cfFileDescPoMapper.insertSelective(cfFileDescPo);

                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
                fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
                fileMoveRecordPo.setMoveStatus("迁移成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("成果号" + mgMapResultPo.getId() + "迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);

                endTime = System.currentTimeMillis();
                System.out.println("关联关系段运行时间：" + (endTime - startTime) + "ms");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * @Author YangFeng
     * @Description //MgDoorImg表文件迁移
     * @Date 17:23 2021/1/21
     * @Param [fileMoveCurrentContext]
     * @return void
     **/
    public void moveMgDoorImgTable(FileMoveCurrentContext fileMoveCurrentContext) {
        String theadId = Thread.currentThread().getName();
        String fileExe = "";
        String storeId = null;
        MgDoorImgPo mgDoorImgPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;

        long startTime=0;
        long endTime=0;

        while (FileMoveServiceImpl.inMoveTime) {

            startTime = System.currentTimeMillis();

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

                endTime = System.currentTimeMillis();
                System.out.println("查询任务段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

//              默认当前迁移任务失败，迁移完成后修改为成功， 保证不再查询到该数据，防止异常数据阻塞迁移线程
                fileMoveRecordPo.setCreatetime(new Date());
                fileMoveRecordPo.setThreadId(theadId);
                fileMoveRecordPo.setMoveStatus("迁移失败");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getCurMoveDataSourceName());

                endTime = System.currentTimeMillis();
                System.out.println("修改任务段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

                mgDoorImgPo = mgDoorImgPoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));

                endTime = System.currentTimeMillis();
                System.out.println("读取大字段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

                ContextSynchronizationManager.bindResource("datasource", fileMoveCurrentContext.getTransitionDBName());
                if (mgDoorImgPo == null) {
                    fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (mgDoorImgPo.getStatus() == null) {
                    fileMoveRecordPo.setRemark("mgDoorImgPo--mgstatus文件状态为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                if (mgDoorImgPo.getImage()== null || mgDoorImgPo.getImage().length == 0) {
                    fileMoveRecordPo.setRemark("mgDoorImgPo--Image字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }
                if (mgDoorImgPo.getImgfilename() == null) {
                    fileMoveRecordPo.setRemark("mgDoorImgPo--Imgfilename文件名称为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                //默认全部为dwg格式
                fileExe = "dwg";
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

                endTime = System.currentTimeMillis();
                System.out.println("文件上传段运行时间：" + (endTime - startTime) + "ms");
                startTime = System.currentTimeMillis();

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
                cfFileDescPo.setStatus(mgDoorImgPo.getStatus().shortValueExact());
                cfFileDescPo.setCreateTime(mgDoorImgPo.getRegidate());
                cfFileDescPo.setCreatorId(mgDoorImgPo.getCreater());
                cfFileDescPoMapper.insertSelective(cfFileDescPo);

                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(cfFileDescPo.getFileStoreId());
                fileMoveRecordPo.setFileSize(cfFileDescPo.getFileSize());
                fileMoveRecordPo.setMoveStatus("迁移成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);

                endTime = System.currentTimeMillis();
                System.out.println("关联关系段运行时间：" + (endTime - startTime) + "ms");

                System.out.println("迁移件：" + mgDoorImgPo.getId() + "迁移完成;文件storeId" + storeId);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * @Author YangFeng
     * @Description //Imgimages表文件迁移
     * @Date 17:23 2021/1/21
     * @Param [fileMoveCurrentContext]
     * @return void
     **/
    public void moveImgimagesTable(FileMoveCurrentContext fileMoveCurrentContext) {
        String theadId = Thread.currentThread().getName();
        String fileExe = "";
        String storeId = null;
        ImgImagesPo imgImagesPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        //文件服务器文件上传连续失败次数，计数，如大于该次数推出当前迁移线程
        int FastUpLoadContinuousFailureCount = 0;

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

                if (imgImagesPo.getImage()== null || imgImagesPo.getImage().length == 0) {
                    fileMoveRecordPo.setRemark("mgDoorImgPo--Image字段为空");
                    fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                    continue;
                }

                fileExe = "dwg";
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
                cfFileDescPo.setFileName(imgImagesPo.getId().toString());
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
                System.out.println("迁移件：" + imgImagesPo.getId() + "迁移完成;文件storeId" + storeId);
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
            executorService.shutdown();
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
        for (int i = 0; i < Constants.CPU_CORE_SIZE_IO; i++) {
            executorService.submit(this::fileMove);
        }
        return true;
    }

}
