package com.funi.filemove.determinedatasource;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ContextSynchronizationManager
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 17:51
 * @Version 1.0
 */
public class ContextSynchronizationManager {
    private static final ThreadLocal<Map<Object, Object>> resources =
            new ThreadLocal<Map<Object, Object>>();

    /**
     * 绑定线程相关资源
     * @param key
     * @param value
     */
    public static void bindResource(Object key, Object value) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<Object, Object>();
            resources.set(map);
        }
        map.put(key, value);
    }

    /**
     * 获取线程先关资源
     * @param key
     * @return
     */
    public static Object getResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return  value;
    }

    /**
     * 解除线程相关资源绑定
     * @param key
     * @return
     */
    public static Object unbindResource(Object key) {
        Map<Object, Object> map = resources.get();
        if (map == null) {
            return null;
        }
        Object value = map.remove(key);
        if (map.isEmpty()) {
            resources.remove();
        }
        return  value;
    }
}
