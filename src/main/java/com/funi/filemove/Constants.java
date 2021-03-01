package com.funi.filemove;

import sun.security.action.PutAllAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Constants
 * @Description 常量值
 * @Author Feng.Yang
 * @Date 2020/11/5 14:26
 * @Version 1.0
 */
public final class Constants {
    private Constants(){

    }

//    private static final int CPU_CORE_SIZE=Runtime.getRuntime().availableProcessors();

//    public static final int CPU_CORE_SIZE_IO=CPU_CORE_SIZE<<1;

//    private static final int CPU_CORE_SIZE=Runtime.getRuntime().availableProcessors();

    public static final int CPU_CORE_SIZE_IO=1;

    public static final String SYSTEM_CODE="surveyInner";

    public static final String FAST_DFS_PREFIX = "fastdfs://";

    public static List<Map<String,String>> dataSourceList=null;

    public static List<Map<String,String>> tableNameList=null;

    public static final String MOVE_RECORD_STATUS="未迁移";

    public static final String DEFAULT_DATA_SOURCE_NAME="zj";

    public static final int MAX_RETRY_TIMES=3;

    public static final String MG_MAP_FIGURE="mgmapfigure";
    public static final String MG_DOOR_IMG="mgdoorimg";
    public static final String IMG_IMAGES="imgimages";

    static {
        dataSourceList=new ArrayList<Map<String,String>>(){{
//            add(new HashMap<String,String>() {{ put("dataSourceName","cd");put("dataSource","510100");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","tfxq");put("dataSource","510142");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","lq");put("dataSource","510112");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","sl");put("dataSource","510122");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","qbj");put("dataSource","510113");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","wj");put("dataSource","510115");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","px");put("dataSource","510124");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","xd");put("dataSource","510114");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","jy");put("dataSource","510180");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","jt");put("dataSource","510121");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","dy");put("dataSource","510129");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","pj");put("dataSource","510131");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","xj");put("dataSource","510132");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","djy");put("dataSource","510181");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","pz");put("dataSource","510182");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","ql");put("dataSource","510183");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","cz");put("dataSource","510184");}});
//            add(new HashMap<String,String>() {{ put("dataSourceName","dbxq");put("dataSource","510118");}});
        }};

        tableNameList=new ArrayList<Map<String,String>>(){{
            add(new HashMap<String,String>() {{ put("tableName",Constants.MG_MAP_FIGURE);}});
            add(new HashMap<String,String>() {{ put("tableName",Constants.MG_DOOR_IMG);}});
            add(new HashMap<String,String>() {{ put("tableName",Constants.IMG_IMAGES);}});
        }};


    }
}
