package com.funi.filemove;

import com.funi.filemove.service.FileMoveService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@SpringBootTest
class FilemoveApplicationTests {
    @Resource
    private FileMoveService fileMoveService;
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


//    @Test
//    void contextLoads() {
//        System.out.println(df.format(System.currentTimeMillis())+"---迁移开始");
//        fileMoveService.startMove();
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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

}
