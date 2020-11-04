package com.funi.filemove.service.impl;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.annotation.DataSource;
import com.funi.filemove.dao.CfFileDescPoMapper;
import com.funi.filemove.dao.CommonMapper;
import com.funi.filemove.dao.MgMapFigurePoMapper;
import com.funi.filemove.dao.MgMapResultPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.movethread.MyThreadFacthory;
import com.funi.filemove.po.CfFileDescPo;
import com.funi.filemove.po.MgMapFigurePo;
import com.funi.filemove.po.MgMapResultPo;
import com.funi.filemove.service.FileMoveService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * @ClassName FileMoveServiceImpl
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/10/29 14:33
 * @Version 1.0
 */
@Transactional
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
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;


    public static final String FAST_DFS_PREFIX = "fastdfs://";
    public static final String SYSTEM_CODE = "surveyInner";
    private static Integer pageNum = 1;
    private static Integer pageSize = 20;

    private static volatile boolean inMoveTime = false;
    private static ExecutorService executorService = null;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
//    @DataSource("cd")
    public void testquery() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (inMoveTime) {
            System.err.println(df.format(System.currentTimeMillis()));
            fileMoveStart();
        }

//        ContextSynchronizationManager.bindResource("datasource","lq");
//        Map<String, Object> queryParams = new HashMap<>();
//        PageHelper.startPage(pageNum, pageSize);
//        List<MgMapResultPo> mgMapResultPos = mgMapResultPoMapper.selectMgResultListByFileQuery(queryParams);
//        mgMapResultPos.forEach(item->{
//            System.out.println("id:"+item.getId()+"-----"+item.getProjname());
//        });
    }


    public void test2() {
        while (inMoveTime) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(Thread.currentThread().getName() + ":" + df.format(System.currentTimeMillis()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fileMoveStart() {
        TransactionStatus transactionStatus = null;
        String theadId = null;
        while (FileMoveServiceImpl.inMoveTime) {
            try {
                //手动开启事务
                transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
                theadId = Thread.currentThread().getName();
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("theadId", Integer.valueOf(theadId));ContextSynchronizationManager.bindResource("datasource","lq");
                MgMapFigurePo mgMapFigurePo = mgMapFigurePoMapper.selectFileByFileQuery(queryParams);
                if (mgMapFigurePo == null) {
                    System.out.println("数据迁移完毕！！！");
                    return;
                }

                InputStream fileInputStream = new ByteArrayInputStream(mgMapFigurePo.getImage());
                String s = fastDfsFileUpload.fileUpload(fileInputStream, mgMapFigurePo.getImgstyle());
                //根据mgMapFigurePo 查询对应的成果
                MgMapResultPo mgMapResultPo = mgMapResultPoMapper.selectByPrimaryKey(mgMapFigurePo.getResultsid());
                //判断是否有对应的 成果业务件
                if (mgMapResultPo == null) {
                    continue;
                }

                //通过mgMapFigure id及其对应的成果业务件uuid 查询是都已经存 对应的文件关联关系
                Map<String, Object> fileQueryParams = new HashMap<>();
                fileQueryParams.put("busUuid", mgMapResultPo.getUuid());
                fileQueryParams.put("storeId", mgMapFigurePo.getId().toString());
                //可能存在一个mgMapFigure id 被同一个 成果业务件关联多次的 问题  所用列表接收
                ContextSynchronizationManager.bindResource("datasource","cd");
                List<CfFileDescPo> cfFileDescPos = cfFileDescPoMapper.selectFileListByFigIdAndBusUuid(fileQueryParams);
                //存在就修改FileStoreId 为文件服务器文件地址 否则新建关联关系
                if (cfFileDescPos != null && cfFileDescPos.size() > 0) {
                    for (CfFileDescPo cfFileDescPo : cfFileDescPos) {
                        cfFileDescPo.setFileStoreId(FileMoveServiceImpl.FAST_DFS_PREFIX + s);
                        cfFileDescPoMapper.updateByPrimaryKeySelective(cfFileDescPo);
                    }
                } else {
                    CfFileDescPo cfFileDescPo = new CfFileDescPo();
                    cfFileDescPo.setUuid(commonMapper.selectSystemUUid());
                    cfFileDescPo.setFileStoreId(FileMoveServiceImpl.FAST_DFS_PREFIX + s);
                    cfFileDescPo.setStoreType((short) 2);//2表明该文件 是fastdfs文件
                    cfFileDescPo.setFileSize(mgMapFigurePo.getImgfilesize());
                    cfFileDescPo.setFileName(mgMapFigurePo.getImagename());
                    cfFileDescPo.setExtName(mgMapFigurePo.getImgstyle());
                    cfFileDescPo.setIsUse((short) 1);
                    cfFileDescPo.setSystemCode(FileMoveServiceImpl.SYSTEM_CODE);
                    cfFileDescPo.setBusinessTable(null);
                    cfFileDescPo.setBusinessUuid(mgMapResultPo.getUuid());
                    cfFileDescPo.setStatus((short) 1);
                    cfFileDescPo.setCreateTime(new Date());
                    cfFileDescPo.setCreatorId("老文件迁入");
                    cfFileDescPoMapper.insertSelective(cfFileDescPo);
                }
            } catch (Exception e) {
                //手动回滚事务
                dataSourceTransactionManager.rollback(transactionStatus);//最好是放在catch 里面,防止程序异常而事务一直卡在哪里未提交
            }
//          手动提交事务
            dataSourceTransactionManager.commit(transactionStatus);//提交
        }
    }


    @Override
    public boolean stopMove() {
        //立即停止
        if (executorService != null && !executorService.isShutdown()) {
            FileMoveServiceImpl.inMoveTime = false;
            executorService.shutdown();
        }
        System.out.println("123");
        return true;
    }

    @Override
    public boolean startMove() {
        MyThreadFacthory threadFactory = new MyThreadFacthory();
        executorService = Executors.newFixedThreadPool(6, threadFactory);
        FileMoveServiceImpl.inMoveTime = true;
        for (int i = 0; i < 6; i++) {
            executorService.submit(this::test2);
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
