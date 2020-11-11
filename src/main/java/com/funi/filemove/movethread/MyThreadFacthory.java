package com.funi.filemove.movethread;

import com.funi.filemove.Constants;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName MyThreadFacthory
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/3 11:08
 * @Version 1.0
 */
public class MyThreadFacthory implements ThreadFactory {

    private static AtomicInteger currentThreadId=new AtomicInteger(0);
    @Override
    public Thread newThread(Runnable r) {
        if (currentThreadId.get()>= Constants.CPU_CORE_SIZE_IO){
            return null;
        }
        Thread thread=new Thread(r,currentThreadId.incrementAndGet()+"");
        thread.setDaemon(true);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}
