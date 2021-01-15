package com.funi.filemove;

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
//    void createTaskDataTest(){
//        fileMoveService.createTaskData();
//    }

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

    @Test
    void testDataClear(){
        try {
            fileMoveService.testDataClear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
//    void fileDownLoad(){
//
////        fileMoveService.fileDownLoad();
//
//    }

}
