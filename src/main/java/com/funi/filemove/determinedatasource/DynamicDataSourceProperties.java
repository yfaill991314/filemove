package com.funi.filemove.determinedatasource;

/**
 * @ClassName DynamicDataSourceProperties
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 13:39
 * @Version 1.0
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多数据源属性 在application中表示为以 dynamic 为节点的配置
 *
 */
//@Component
@ConfigurationProperties(prefix = "dynamic")
public class DynamicDataSourceProperties {
    private Map<String, DataSourceProperties> datasource = new LinkedHashMap<>();
    private Map<String, JndiDataSourceProperties> jndidatasource=new LinkedHashMap<>();

    public Map<String, DataSourceProperties> getDatasource() {
        return datasource;
    }
    public void setDatasource(Map<String, DataSourceProperties> datasource) {
        this.datasource = datasource;
    }

    public Map<String, JndiDataSourceProperties> getJndidatasource() {
        return jndidatasource;
    }

    public void setJndidatasource(Map<String, JndiDataSourceProperties> jndidatasource) {
        this.jndidatasource = jndidatasource;
    }
}
