package com.funi.filemove.determinedatasource;

/**
 * @ClassName DynamicDataSourceConfig
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 13:47
 * @Version 1.0
 */


import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置多数据源
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceConfig {
    @Autowired
    private DynamicDataSourceProperties properties;

    @Bean
    public DynamicDataSource dynamicDataSource() throws Exception {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        Map<Object, Object> dynamicDataSourceMap = getDynamicDataSource();
        dynamicDataSource.setTargetDataSources(dynamicDataSourceMap);

        //默认数据源
//        DataSource defaultXaDataSource = getdefaultDynamicDataSource();
        dynamicDataSourceMap.forEach((k, v) -> {
            if ("zj".equals(k)){
                dynamicDataSource.setDefaultTargetDataSource(v);
            }
        });
        return dynamicDataSource;
    }

    @Bean(name = "jtaTransactionManager")
    public JtaTransactionManager regTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        UserTransaction userTransaction = new UserTransactionImp();
        return new JtaTransactionManager(userTransaction, userTransactionManager);
    }


//    private DataSource getdefaultDynamicDataSource() throws Exception {
//        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
//        DataSource defaultXaDataSource =null;
//        for (Map.Entry<String, DataSourceProperties> entry:dataSourcePropertiesMap.entrySet()){
//            if ("cd".equals(entry.getKey())){
//                defaultXaDataSource = DynamicDataSourceFactory.buildAtomikosDataSource(entry.getKey(),entry.getValue());
//            }
//        }
//        if (defaultXaDataSource==null){
//            throw new Exception("default DataSource is null");
//        }
//        return defaultXaDataSource;
//    }

    private Map<Object, Object> getDynamicDataSource() {
        Map<String, DataSourceProperties> dataSourcePropertiesMap = properties.getDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>(dataSourcePropertiesMap.size());
        dataSourcePropertiesMap.forEach((k, v) -> {
            DataSource xaDataSource = DynamicDataSourceFactory.buildAtomikosDataSource(k, v);
            targetDataSources.put(k, xaDataSource);
        });

        return targetDataSources;
    }

}
