package com.funi.filemove.movethread;

import java.util.concurrent.ThreadFactory;

/**
 * @ClassName MyThreadFacthory
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/3 11:08
 * @Version 1.0
 */
public class MyThreadFacthory implements ThreadFactory {
    private static Integer currentThreadId=0;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread=new Thread(r,currentThreadId.toString());
        currentThreadId++;
        thread.setDaemon(true);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}
