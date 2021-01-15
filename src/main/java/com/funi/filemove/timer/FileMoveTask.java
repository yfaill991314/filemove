package com.funi.filemove.timer;

import com.funi.filemove.service.FileMoveService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @ClassName SaticScheduleTask
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/10/29 10:14
 * @Version 1.0
 */
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class FileMoveTask {
    @Resource
    private FileMoveService fileMoveService;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//    //3.添加定时任务
//    @Scheduled(cron = "0/5 * * * * ?")
//
////    每天22-24,0-6点 每五分钟执行一次
////    @Scheduled(cron = "0 0/5 22-23,0-5 * * ?")
//    //或直接指定时间间隔，例如：5秒
////    @Scheduled(fixedRate=5000)
//    private void configureTasks() {
////        System.err.println(df.format(System.currentTimeMillis()));
//
//        fileMoveService.testquery();
//    }





    @Scheduled(cron = "0 0,20,40 * * * ?")
    public void startFileMoveTasks() {
//        System.out.println(df.format(System.currentTimeMillis())+"---迁移开始");
//        fileMoveService.startMove();
    }

    @Scheduled(cron = "0 17,37,57 * * * ?")
    public void stopFileMoveTasks() {
//        fileMoveService.stopMove();
//        System.out.println(df.format(System.currentTimeMillis())+"---迁移结束");
    }

}
