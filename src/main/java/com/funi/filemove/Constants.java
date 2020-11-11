package com.funi.filemove;

import sun.security.action.PutAllAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Constants
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/5 14:26
 * @Version 1.0
 */
public final class Constants {
    private Constants(){

    }

    private static final int CPU_CORE_SIZE=Runtime.getRuntime().availableProcessors();

    public static final int CPU_CORE_SIZE_IO=CPU_CORE_SIZE<<1;

    public static final String SYSTEM_CODE="surveyInner";

    public static final String FAST_DFS_PREFIX = "fastdfs://";

    public static List<Map<String,String>> dataSource=null;

    static {
        dataSource=new ArrayList<Map<String,String>>(){{
            add(new HashMap<String,String>() {{ put("dataSourceName","cd");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","lq");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","qbj");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","xd");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","wj");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","jt");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","sl");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","px");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","dy");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","pj");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","xj");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","tf");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","jy");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","djy");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","pz");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","ql");}});
            add(new HashMap<String,String>() {{ put("dataSourceName","cz");}});
        }};
    }
}
