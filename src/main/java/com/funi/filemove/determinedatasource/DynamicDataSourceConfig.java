package com.funi.filemove.determinedatasource;

/**
 * @ClassName DynamicDataSourceConfig
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 13:47
 * @Version 1.0
 */

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置多数据源
 *
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfig {
    @Autowired
    private DynamicDataSourceProperties properties;

//    @Bean
//    @ConfigurationProperties(prefix = "spring.datasource.druid")
//    public DataSourceProperties dataSourceProperties() {
//        return new DataSourceProperties();
//    }

    @Bean
    public DynamicDataSource dynamicDataSource() throws Exception {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(getDynamicDataSource());

        //默认数据源
        DruidDataSource defaultDataSource = getdefaultDynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }

    private DruidDataSource getdefaultDynamicDataSource() throws Exception {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        DruidDataSource defaultDruidDataSource =null;
        for (Map.Entry<String, DataSourceProperties> entry:dataSourcePropertiesMap.entrySet()){
            if ("cd".equals(entry.getKey())){
                defaultDruidDataSource = DynamicDataSourceFactory.buildDruidDataSource(entry.getValue());
            }
        }
        if (defaultDruidDataSource==null){
            throw new Exception("default DataSource is null");
        }
        return defaultDruidDataSource;
    }

    private Map<Object, Object> getDynamicDataSource(){
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourcePropertiesMap.size());
        dataSourcePropertiesMap.forEach((k, v) -> {
            DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(v);
            targetDataSources.put(k, druidDataSource);
        });

        return targetDataSources;
    }

}
