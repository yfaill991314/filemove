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
import org.apache.logging.log4j.message.ReusableMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileMoveRecordServiceImpl implements FileMoveRecordService {
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;
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
        ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
        List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryParams);
        PageInfo<FileMoveRecordPo> pageInfo = new PageInfo<>(fileMoveRecordPos);
        long total = pageInfo.getTotal();
        Map<String, Object> result = new HashMap<>();
        result.put("rows", fileMoveRecordPos);
        result.put("total", total);
        return result;
    }

    @Override
    public void importTaskTable(Map<String, Object> queryParams) {
        List<Map<String, String>> tableNameList = Constants.tableNameList;
        List<Map<String, String>> dataSourceList = Constants.dataSourceList;

        for (Map<String, String> tableNameItem : tableNameList) {
            if (!tableNameItem.get("tableName").equals(queryParams.get("tableName"))) {
                continue;
            }
            for (Map<String, String> dataSourceItem : dataSourceList) {
                if (!dataSourceItem.get("dataSource").equals(queryParams.get("dataSource"))) {
                    continue;
                }
                this.importCurrentTable(tableNameItem, dataSourceItem);
            }
        }
    }

    private void importCurrentTable(Map<String, String> tableNameItem, Map<String, String> dataSourceItem) {
        int pageSize = 5000;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long startTime = 0;
        long endTime = 0;

        startTime = System.currentTimeMillis();
        while (true) {
            System.out.println(df.format(new Date()));
            ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
            Map<String, Object> recordQueryMap = new HashMap<>();
            recordQueryMap.put("dataSource", dataSourceItem.get("dataSource"));
            recordQueryMap.put("tableName", tableNameItem.get("tableName"));
            BigDecimal bizId = fileMoveRecordPoMapper.selectMaxBizIdByParams(recordQueryMap);
            if (bizId == null) {
                bizId = new BigDecimal(0);
            }
            System.out.println(df.format(new Date()));

            ContextSynchronizationManager.bindResource("datasource", dataSourceItem.get("dataSourceName"));
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("minId", bizId);
            queryMap.put("pageSize", pageSize);
            List<FileMoveRecordPo> fileMoveRecordPos = this.getFileMoveRecordPos(tableNameItem, dataSourceItem, queryMap);
            if (fileMoveRecordPos == null || fileMoveRecordPos.size() <= 0) {
                System.out.println("数据源:" + dataSourceItem.get("dataSource") + "--数据表:" + tableNameItem.get("tableName") + "任务导入完毕！");
                break;
            }
            System.out.println(df.format(new Date()));

            ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
            fileMoveRecordPoMapper.insertAll(fileMoveRecordPos);
            System.out.println(df.format(new Date()));
            System.out.println("数据源:" + dataSourceItem.get("dataSource") + "--数据表:" + tableNameItem.get("tableName") + "---业务件" + fileMoveRecordPos.get(0).getBizid().toString() + "至" + fileMoveRecordPos.get(fileMoveRecordPos.size() - 1).getBizid().toString() + "共计:" + fileMoveRecordPos.size() + "条，---导入完成");
        }
        endTime = System.currentTimeMillis();
        System.out.println("总运行时间：" + (endTime - startTime) / 1000 + "s");
    }

    private List<FileMoveRecordPo> getFileMoveRecordPos(Map<String, String> tableNameItem, Map<String, String> dataSourceItem, Map<String, Object> queryMap) {
        List<FileMoveRecordPo> fileMoveRecordPoList = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if ("mgmapfigure".equals(tableNameItem.get("tableName"))) {
            List<MgMapFigurePo> mgMapFigurePos = mgMapFigurePoMapper.selectListFigureByMinId(queryMap);
            for (MgMapFigurePo mgMapFigurePo : mgMapFigurePos) {
                FileMoveRecordPo fileMoveRecordPo = new FileMoveRecordPo();
                fileMoveRecordPo.setUuid(MyUtils.getUuid36());
                fileMoveRecordPo.setBizid(mgMapFigurePo.getId().toString());
                fileMoveRecordPo.setTablename(tableNameItem.get("tableName"));
                fileMoveRecordPo.setDataSource(dataSourceItem.get("dataSource"));
                fileMoveRecordPo.setMoveStatus(Constants.MOVE_RECORD_STATUS);
                fileMoveRecordPoList.add(fileMoveRecordPo);
            }
        } else if ("mgdoorimg".equals(tableNameItem.get("tableName"))) {
            List<MgDoorImgPo> mgDoorImgPoList = mgDoorImgPoMapper.selectListDoorImgByMinId(queryMap);
            for (MgDoorImgPo mgDoorImgPo : mgDoorImgPoList) {
                FileMoveRecordPo fileMoveRecordPo = new FileMoveRecordPo();
                fileMoveRecordPo.setUuid(MyUtils.getUuid36());
                fileMoveRecordPo.setBizid(mgDoorImgPo.getId().toString());
                fileMoveRecordPo.setTablename(tableNameItem.get("tableName"));
                fileMoveRecordPo.setDataSource(dataSourceItem.get("dataSource"));
                fileMoveRecordPo.setMoveStatus(Constants.MOVE_RECORD_STATUS);
                fileMoveRecordPoList.add(fileMoveRecordPo);
            }
        } else if ("imgimages".equals(tableNameItem.get("tableName"))) {
            System.out.println("----"+df.format(new Date()));
            List<ImgImagesPo> imgImagesPoList = imgImagesPoMapper.selectListImgImagesByMinId(queryMap);
            System.out.println("----"+df.format(new Date()));
            for (ImgImagesPo imgImagesPo : imgImagesPoList) {
                FileMoveRecordPo fileMoveRecordPo = new FileMoveRecordPo();
                fileMoveRecordPo.setUuid(MyUtils.getUuid36());
                fileMoveRecordPo.setBizid(imgImagesPo.getId().toString());
                fileMoveRecordPo.setTablename(tableNameItem.get("tableName"));
                fileMoveRecordPo.setDataSource(dataSourceItem.get("dataSource"));
                fileMoveRecordPo.setMoveStatus(Constants.MOVE_RECORD_STATUS);
                fileMoveRecordPoList.add(fileMoveRecordPo);
            }
            System.out.println("----"+df.format(new Date()));
        } else {
            System.out.println("未匹配的数据表");
        }
        return fileMoveRecordPoList;
    }

    @Override
    public void clearData(Map<String, Object> queryParams) {
//        UserTransaction userTransaction = jtaTransactionManager.getUserTransaction();
        ContextSynchronizationManager.bindResource("datasource", Constants.DEFAULT_DATA_SOURCE_NAME);
        while (true) {
            try {
//                userTransaction.begin();
                Map<String, Object> queryMap = new HashMap<>();
                PageHelper.startPage(1, 20);
                List<String> MoveStatusList = new ArrayList<String>() {{
                    add("迁移失败");
                    add("迁移成功");
                }};
                queryMap.put("moveStatusList", MoveStatusList);
                queryMap.put("dataSource",queryParams.get("dataSource"));
                queryMap.put("tableName",queryParams.get("tableName"));
                List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryMap);
                if (fileMoveRecordPos == null || fileMoveRecordPos.size() <= 0) {
                    break;
                }
                for (FileMoveRecordPo fileMoveRecordPo : fileMoveRecordPos) {
                    if ("迁移成功".equals(fileMoveRecordPo.getMoveStatus())) {
                        cfFileDescPoMapper.deleteByPrimaryKey(fileMoveRecordPo.getFileUuid());
                        int i = fastDfsFileUpload.fileDelete(fileMoveRecordPo.getFileStoreId());
                    }
                    System.out.println("任务:" + fileMoveRecordPo.getUuid() + "--清理前状态:"+fileMoveRecordPo.getMoveStatus()+"--文件Uuid:" +fileMoveRecordPo.getFileUuid() + "--已删除");
                    fileMoveRecordPo.setFileStoreId(null);
                    fileMoveRecordPo.setFileUuid(null);
                    fileMoveRecordPo.setCreatetime(null);
                    fileMoveRecordPo.setMoveStatus("未迁移");
                    fileMoveRecordPo.setRemark(null);
                    fileMoveRecordPo.setThreadId(null);
                    fileMoveRecordPo.setFileSize(null);
                    fileMoveRecordPoMapper.updateByPrimaryKey(fileMoveRecordPo);
                }
            } catch (Exception e) {
                e.printStackTrace();
//                userTransaction.rollback();
            }
//            userTransaction.commit();
        }
    }

    @Override
    public Map<String, Object> remigrateMgMapFigure(Map<String, Object> queryParams) {
        String fileExe = "";
        String fileTypeName = "";
        String storeId = "";
        MgMapResultPo mgMapResultPo = null;
        MgMapFigurePo mgMapFigurePo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        String dataSorceName = MyUtils.getDataSouceNameByCode(queryParams.get("dataSource").toString());
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

            ContextSynchronizationManager.bindResource("datasource", dataSorceName);
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

            if (mgMapFigurePo.getImagename() == null) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--Imagename文件名称为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }
            if (mgMapFigurePo.getImgstyle() == null) {
                fileMoveRecordPo.setRemark("mgMapFigurePo--Imgstyle文件后缀为空");
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
            cfFileDescPo.setUuid(MyUtils.getUuid36());
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
                ContextSynchronizationManager.bindResource("datasource", "cd");
                CfDictPo cfDictPo = cfDictPoMapper.selectByMapParame(queryMap);
                ContextSynchronizationManager.bindResource("datasource", "zj");
                if (cfDictPo != null) {
                    cfFileDescPo.setBusinessType(cfDictPo.getUuid());
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
            System.out.println("成果号" + mgMapResultPo.getId() + "迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> remigrateMgDoorImg(Map<String, Object> queryParams) {
        String fileExe = "";
        String storeId = null;
        MgDoorImgPo mgDoorImgPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;

        String dataSorceName = MyUtils.getDataSouceNameByCode(queryParams.get("dataSource").toString());
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

            ContextSynchronizationManager.bindResource("datasource", dataSorceName);
            mgDoorImgPo = mgDoorImgPoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
            ContextSynchronizationManager.bindResource("datasource", "zj");


            if (mgDoorImgPo == null) {
                fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            if (mgDoorImgPo.getStatus() == null) {
                fileMoveRecordPo.setRemark("mgDoorImgPo--mgstatus文件状态为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            if (mgDoorImgPo.getImage() == null || mgDoorImgPo.getImage().length == 0) {
                fileMoveRecordPo.setRemark("mgDoorImgPo--Image字段为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            if (mgDoorImgPo.getImgfilename() == null) {
                fileMoveRecordPo.setRemark("mgDoorImgPo--Imgfilename文件名称为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            //默认全部为dwg格式
            fileExe = "dwg";

            storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(mgDoorImgPo.getImage()), fileExe);
            if (storeId == null) {
                fileMoveRecordPo.setRemark("上传文件失败,storeId为空。(请确认文件服务器工作状态)");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("迁移失败:上传文件失败,storeId为空。(请确认文件服务器工作状态");
                return null;
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
            cfFileDescPo.setBusinessTable(null);
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
            System.out.println("迁移件：" + mgDoorImgPo.getId() + "迁移完成;文件storeId" + storeId);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, Object> remigrateImgimages(Map<String, Object> queryParams) {
        String fileExe = "";
        String storeId = null;
        ImgImagesPo imgImagesPo = null;
        FileMoveRecordPo fileMoveRecordPo = null;
        String dataSorceName = MyUtils.getDataSouceNameByCode(queryParams.get("dataSource").toString());
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

            ContextSynchronizationManager.bindResource("datasource", dataSorceName);
            imgImagesPo = imgImagesPoMapper.selectByPrimaryKey(new BigDecimal(fileMoveRecordPo.getBizid()));
            ContextSynchronizationManager.bindResource("datasource", "zj");

            if (imgImagesPo == null) {
                fileMoveRecordPo.setRemark("未查找到对应的被迁移文件");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            if (imgImagesPo.getImage() == null || imgImagesPo.getImage().length == 0) {
                fileMoveRecordPo.setRemark("mgDoorImgPo--Image字段为空");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                return null;
            }

            fileExe = "dwg";
            storeId = fastDfsFileUpload.fileUpload(new ByteArrayInputStream(imgImagesPo.getImage()), fileExe);
            if (storeId == null) {
                fileMoveRecordPo.setRemark("上传文件失败,storeId为空。(请确认文件服务器工作状态)");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("迁移失败:上传文件失败,storeId为空。(请确认文件服务器工作状态");
                return null;
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
            cfFileDescPo.setBusinessTable(null);
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
        return null;
    }
}
