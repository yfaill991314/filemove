package com.funi.filemove.determinedatasource;

/**
 * @ClassName DynamicDataSource
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 14:07
 * @Version 1.0
 */

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 多数据源 继承AbstractRoutingDataSource进行扩展，重新 determineCurrentLookupKey 方法实现多数据源
 *
 * @author  fishpro
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    //获取当前数据库配置中的数据源
    @Override
    protected Object determineCurrentLookupKey() {
        return  ContextSynchronizationManager.getResource("datasource");
        //peek() 获得当前线程数据源
//        return DynamicContextHolder.getDataSource();
    }

}