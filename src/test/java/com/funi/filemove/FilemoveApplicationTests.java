package com.funi.filemove;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.filemove.dao.CfFileDescPoMapper;
import com.funi.filemove.dao.FileMoveRecordPoMapper;
import com.funi.filemove.determinedatasource.ContextSynchronizationManager;
import com.funi.filemove.po.FileMoveRecordPo;
import com.funi.filemove.service.FileMoveService;
import com.github.pagehelper.PageHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class FilemoveApplicationTests {
    @Resource
    private FileMoveService fileMoveService;
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource
    private FileMoveRecordPoMapper fileMoveRecordPoMapper;
    @Resource
    private CfFileDescPoMapper cfFileDescPoMapper;
    @Resource
    private FastDfsFileUpload fastDfsFileUpload;


    @Test
    void startMove() {
        System.out.println(df.format(System.currentTimeMillis())+"---迁移开始");
        fileMoveService.startMove();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
//    void contextLoads2() {
//        Map<String, Object> queryMap = new HashMap<>();
//        PageHelper.startPage(1, 20);
//        ContextSynchronizationManager.bindResource("datasource", "lq");
//        List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryMap);
//        fileMoveRecordPos.forEach(item->{
//            System.out.println(item.getUuid()+"--"+item.getMoveresult());
//        });
//
//    }

//    @Test
//    void testDataClear(){
//        try {
//            fileMoveService.testDataClear();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    @Test
//    void fileDownLoad(){
//
////        fileMoveService.fileDownLoad();
//
//    }

    @Test
    public void clearData() {
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
                List<FileMoveRecordPo> fileMoveRecordPos = fileMoveRecordPoMapper.selectListRecord(queryMap);
                if (fileMoveRecordPos == null || fileMoveRecordPos.size() <= 0) {
                    break;
                }
                for (int i=0;i<fileMoveRecordPos.size();i++) {
                    FileMoveRecordPo fileMoveRecordPo=fileMoveRecordPos.get(i);
                    if ("迁移成功".equals(fileMoveRecordPo.getMoveStatus())) {
                        cfFileDescPoMapper.deleteByPrimaryKey(fileMoveRecordPo.getFileUuid());
                        fastDfsFileUpload.fileDelete(fileMoveRecordPo.getFileStoreId());
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

}
