package com.funi.filemove.utils;

import com.funi.filemove.Constants;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName Utils
 * @Description TODO
 * @Author YangFeng
 * @Date 2021/1/19 20:49
 * @Version 1.0
 */
public class MyUtils {

    public static String getDataSouceNameByCode(String dataSourceCode){
        List<Map<String,String>>  dataSourceList= Constants.dataSourceList;
        for (Map<String,String> dataSource:dataSourceList){
            if (dataSource.get("dataSource").equals(dataSourceCode)){
                return dataSource.get("dataSourceName");
            }
        }
        return null;
    }

    public static String getUuid36(){
        return UUID.randomUUID().toString();
    }
}
