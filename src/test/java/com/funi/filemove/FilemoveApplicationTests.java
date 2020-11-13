package com.funi.filemove;

import com.funi.filemove.service.FileMoveService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class FilemoveApplicationTests {
    @Resource
    private FileMoveService fileMoveService;



//    @Test
//    void contextLoads() {
////        fileMoveService.testquery();
//        try {
//            fileMoveService.fileMove();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
