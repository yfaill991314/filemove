package com.funi.filemove.service.impl;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.Constants;
import com.funi.filemove.dao.*;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.movethread.MyThreadFacthory;
import com.funi.filemove.po.*;
import com.funi.filemove.service.DataSourceChange;
import com.funi.filemove.service.FileMoveService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.annotation.Resource;
import javax.transaction.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @ClassName FileMoveServiceImpl
 * @Description TODO
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
    private MgMapResultPoMapper mgMapResultPoMapper;
    @Resource
    private CommonMapper commonMapper;
    @Resource
    private CfDictPoMapper cfDictPoMapper;
    @Resource
    private JtaTransactionManager jtaTransactionManager;
    //    @Resource
//    private UserTransaction userTransaction;
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;
    @Resource
    private DataSourceChange dataSourceChange;


    private static volatile boolean inMoveTime = false;
    private static ExecutorService executorService = null;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public void testDataClear() throws Exception {
        UserTransaction userTransaction = jtaTransactionManager.getUserTransaction();
        while (true) {
            try {
                userTransaction.begin();
                Map<String, Object> queryMap = new HashMap<>();
                PageHelper.startPage(1, 20);
                ContextSynchronizationManager.bindResource("datasource", "lq");
                List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryMap);
                if (fileMoveRecordPos == null || fileMoveRecordPos.size() <= 0) {
                    break;
                }
                for (FileMoveRecordPo fileMoveRecordPo : fileMoveRecordPos) {
                    if ("成功".equals(fileMoveRecordPo.getMoveresult())) {
                        ContextSynchronizationManager.bindResource("datasource", "cd");
                        cfFileDescPoMapper.deleteByPrimaryKey(fileMoveRecordPo.getFileUuid());
                        int i = fastDfsFileUpload.fileDelete(fileMoveRecordPo.getFileStoreId());
                    }
                    ContextSynchronizationManager.bindResource("datasource", "lq");
                    fileMoveRecordPoMapper.deleteByPrimaryKey(fileMoveRecordPo.getUuid());
                }
            } catch (Exception e) {
                userTransaction.rollback();
            }
            userTransaction.commit();

        }


    }


    @Override
    public void fileMove() {
        String theadId = Thread.currentThread().getName();
        UserTransaction userTransaction = jtaTransactionManager.getUserTransaction();
        String fileExe = "";
        String fileTypeName = "";

        while (FileMoveServiceImpl.inMoveTime) {
            Map<String, Object> queryParams = new HashMap<>();
            //减1才能查询到 模为0的数据
            queryParams.put("theadId", Integer.valueOf(theadId) - 1);
            queryParams.put("theadSum", Constants.CPU_CORE_SIZE_IO);
            ContextSynchronizationManager.bindResource("datasource", DataSourceChangeImpl.getCurrentMoveDataSource());
            MgMapFigurePo mgMapFigurePo = mgMapFigurePoMapper.selectFileByFileQuery(queryParams);
            if (mgMapFigurePo == null) {
                System.out.println(DataSourceChangeImpl.getCurrentMoveDataSource() + "数据源----" + "线程:" + theadId + ":数据已迁移完毕！！！");
                return;
            }


//          无论迁移是否成功都向日志表中写入该数据，保证不再查询到该数据，防止异常数据阻塞迁移线程
            FileMoveRecordPo fileMoveRecordPo = new FileMoveRecordPo();
            fileMoveRecordPo.setUuid(commonMapper.selectSystemUUid());
            fileMoveRecordPo.setTablename("mgmapfigure");
            fileMoveRecordPo.setBizid(mgMapFigurePo.getId().toString());
            fileMoveRecordPo.setCreatetime(new Date());
            fileMoveRecordPo.setThreadId(theadId);
            fileMoveRecordPo.setMoveresult("失败");
            if (mgMapFigurePo.getResultsid() == null) {
                fileMoveRecordPo.setRemark("Resultsid为空");
                fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);
                continue;
            } else if (mgMapFigurePo.getImage() == null) {
                fileMoveRecordPo.setRemark("文件不存在,文件大字段为空");
                fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);
                continue;
            }
            fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);

            //根据mgMapFigurePo 查询对应的成果
            MgMapResultPo mgMapResultPo = mgMapResultPoMapper.selectByPrimaryKey(mgMapFigurePo.getResultsid());
            //判断是否有对应的 成果业务件 没有对应成果 件放弃迁移
            if (mgMapResultPo == null) {
                fileMoveRecordPo.setRemark("未查找到对应成果");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("成果不存在");
                continue;
            }

            InputStream fileInputStream = new ByteArrayInputStream(mgMapFigurePo.getImage());
            fileExe = mgMapFigurePo.getImgstyle();
            if (fileExe != null) {
                int lc = fileExe.lastIndexOf(".");
                if (lc >= 0) {
                    fileExe = fileExe.substring(lc + 1);
                }
            }
            String storeId = Constants.FAST_DFS_PREFIX + fastDfsFileUpload.fileUpload(fileInputStream, fileExe);

            try {
                //开启事务
                userTransaction.begin();
                //通过mgMapFigure id及其对应的成果业务件uuid 查询是都已经存 对应的文件关联关系
                Map<String, Object> fileQueryParams = new HashMap<>();
                fileQueryParams.put("busUuid", mgMapResultPo.getUuid());
                fileQueryParams.put("storeId", mgMapFigurePo.getId().toString());
                ContextSynchronizationManager.bindResource("datasource", "cd");
                CfFileDescPo cfFileDescPo = cfFileDescPoMapper.selectFileListByFigIdAndBusUuid(fileQueryParams);
                //存在就修改FileStoreId 为文件服务器文件地址 否则新建关联关系
                if (cfFileDescPo != null) {
                    cfFileDescPo.setFileStoreId(storeId);
                    cfFileDescPoMapper.updateByPrimaryKeySelective(cfFileDescPo);
                } else {
                    cfFileDescPo = new CfFileDescPo();
                    cfFileDescPo.setUuid(commonMapper.selectSystemUUid());
                    cfFileDescPo.setFileStoreId(storeId);
                    cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                    cfFileDescPo.setFileSize(mgMapFigurePo.getImgfilesize());
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
                        Map<String, Object> queryMap = new HashMap<String, Object>() {{
                            put("fileTypeName", mgMapFigurePo.getImagetype());
                        }};
                        CfDictPo cfDictPo = cfDictPoMapper.selectByMapParame(queryMap);
                        if (cfDictPo != null) {
                            cfFileDescPo.setBusinessType(cfDictPo.getUuid());
                        }
                    }

                    cfFileDescPo.setStatus((short) 1);
                    cfFileDescPo.setCreateTime(mgMapFigurePo.getRegidate());
                    cfFileDescPo.setCreatorId("历史文件迁入");
                    cfFileDescPoMapper.insertSelective(cfFileDescPo);
                }

                ContextSynchronizationManager.bindResource("datasource", DataSourceChangeImpl.getCurrentMoveDataSource());
                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(storeId);
                fileMoveRecordPo.setMoveresult("成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("成果号" + mgMapResultPo.getId() + "迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);
                //手动提交事务
                userTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                //手动回滚事务
                try {
                    userTransaction.rollback();
                } catch (SystemException systemException) {
                    systemException.printStackTrace();
                }
            }
        }
    }

    @Override
    public void secondFileMove() {
        String theadId = Thread.currentThread().getName();
        UserTransaction userTransaction = jtaTransactionManager.getUserTransaction();
        String fileExe = "";
        String fileTypeName = "";

        while (FileMoveServiceImpl.inMoveTime) {
            Map<String, Object> queryParams = new HashMap<>();
            //减1才能查询到 模为0的数据
            queryParams.put("theadId", Integer.valueOf(theadId) - 1);
            queryParams.put("theadSum", Constants.CPU_CORE_SIZE_IO);
            ContextSynchronizationManager.bindResource("datasource", DataSourceChangeImpl.getCurrentMoveDataSource());
            MgMapFigurePo mgMapFigurePo = mgMapFigurePoMapper.selectFileByFileQuery(queryParams);
            if (mgMapFigurePo == null) {
                System.out.println(DataSourceChangeImpl.getCurrentMoveDataSource() + "数据源----" + "线程:" + theadId + ":数据已迁移完毕！！！");
                return;
            }


//          无论迁移是否成功都向日志表中写入该数据，保证不再查询到该数据，防止异常数据阻塞迁移线程
            FileMoveRecordPo fileMoveRecordPo = new FileMoveRecordPo();
            fileMoveRecordPo.setUuid(commonMapper.selectSystemUUid());
            fileMoveRecordPo.setTablename("mgmapfigure");
            fileMoveRecordPo.setBizid(mgMapFigurePo.getId().toString());
            fileMoveRecordPo.setCreatetime(new Date());
            fileMoveRecordPo.setThreadId(theadId);
            fileMoveRecordPo.setMoveresult("失败");
            if (mgMapFigurePo.getResultsid() == null) {
                fileMoveRecordPo.setRemark("Resultsid为空");
                fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);
                continue;
            } else if (mgMapFigurePo.getImage() == null) {
                fileMoveRecordPo.setRemark("文件不存在,文件大字段为空");
                fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);
                continue;
            }
            fileMoveRecordPoMapper.insertSelective(fileMoveRecordPo);

            //根据mgMapFigurePo 查询对应的成果
            MgMapResultPo mgMapResultPo = mgMapResultPoMapper.selectByPrimaryKey(mgMapFigurePo.getResultsid());
            //判断是否有对应的 成果业务件 没有对应成果 件放弃迁移
            if (mgMapResultPo == null) {
                fileMoveRecordPo.setRemark("未查找到对应成果");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("成果不存在");
                continue;
            }

            fileExe = mgMapFigurePo.getImgstyle();
            if (fileExe != null) {
                int lc = fileExe.lastIndexOf(".");
                if (lc >= 0) {
                    fileExe = fileExe.substring(lc + 1);
                }
            }
            String storeId = Constants.FAST_DFS_PREFIX + fastDfsFileUpload.fileUpload(new ByteArrayInputStream(mgMapFigurePo.getImage()), fileExe);

            try {
                //开启事务
                userTransaction.begin();
                //通过mgMapFigure id及其对应的成果业务件uuid 查询是都已经存 对应的文件关联关系
                Map<String, Object> fileQueryParams = new HashMap<>();
                fileQueryParams.put("busUuid", mgMapResultPo.getUuid());
                fileQueryParams.put("storeId", mgMapFigurePo.getId().toString());
                ContextSynchronizationManager.bindResource("datasource", "cd");
                CfFileDescPo cfFileDescPo = cfFileDescPoMapper.selectFileListByFigIdAndBusUuid(fileQueryParams);
                //存在就修改FileStoreId 为文件服务器文件地址 否则新建关联关系
                if (cfFileDescPo != null) {
                    cfFileDescPo.setFileStoreId(storeId);
                    cfFileDescPoMapper.updateByPrimaryKeySelective(cfFileDescPo);
                } else {
                    cfFileDescPo = new CfFileDescPo();
                    cfFileDescPo.setUuid(commonMapper.selectSystemUUid());
                    cfFileDescPo.setFileStoreId(storeId);
                    cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                    cfFileDescPo.setFileSize(mgMapFigurePo.getImgfilesize());
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
                        Map<String, Object> queryMap = new HashMap<String, Object>() {{
                            put("fileTypeName", mgMapFigurePo.getImagetype());
                        }};
                        CfDictPo cfDictPo = cfDictPoMapper.selectByMapParame(queryMap);
                        if (cfDictPo != null) {
                            cfFileDescPo.setBusinessType(cfDictPo.getUuid());
                        }
                    }

                    cfFileDescPo.setStatus((short) 1);
                    cfFileDescPo.setCreateTime(mgMapFigurePo.getRegidate());
                    cfFileDescPo.setCreatorId("历史文件迁入");
                    cfFileDescPoMapper.insertSelective(cfFileDescPo);
                }

                ContextSynchronizationManager.bindResource("datasource", DataSourceChangeImpl.getCurrentMoveDataSource());
                fileMoveRecordPo.setFileUuid(cfFileDescPo.getUuid());
                fileMoveRecordPo.setFileStoreId(storeId);
                fileMoveRecordPo.setMoveresult("成功");
                fileMoveRecordPoMapper.updateByPrimaryKeySelective(fileMoveRecordPo);
                System.out.println("线程："+theadId+"成果号:" + mgMapResultPo.getId() + "迁移件：" + mgMapFigurePo.getId() + "迁移完成;文件storeId" + storeId);
                //手动提交事务
                userTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
                //手动回滚事务
                try {
                    userTransaction.rollback();
                } catch (SystemException systemException) {
                    systemException.printStackTrace();
                }
            }
        }
    }

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

    @Override
    public boolean startMove() {
        dataSourceChange.setTodayMoveDataSource();

        MyThreadFacthory threadFactory = new MyThreadFacthory();
//        executorService = Executors.newFixedThreadPool(Constants.CPU_CORE_SIZE_IO, threadFactory);
        executorService = new ThreadPoolExecutor(Constants.CPU_CORE_SIZE_IO, Constants.CPU_CORE_SIZE_IO,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        FileMoveServiceImpl.inMoveTime = true;
        for (int i = 0; i < Constants.CPU_CORE_SIZE_IO; i++) {
//            executorService.submit(this::test2);
            executorService.submit(this::fileMove);
        }
        return true;
    }

}
