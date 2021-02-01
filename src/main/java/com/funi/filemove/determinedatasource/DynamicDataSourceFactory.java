package com.funi.filemove.determinedatasource;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @ClassName DynamicDataSourceFactory
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/11/2 13:46
 * @Version 1.0
 */
public class DynamicDataSourceFactory {
    /**
     * 通过自定义建立 Druid的数据源
     * */
    public static DataSource buildAtomikosDataSource(String dataSourceName,DataSourceProperties properties) {
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        Properties prop = new Properties();
        prop.put("URL",properties.getUrl());
        prop.put("user",properties.getUsername());
        prop.put("password",properties.getPassword());
        xaDataSource.setXaProperties(prop);

        xaDataSource.setUniqueResourceName("xaDataSource"+dataSourceName);
        xaDataSource.setXaDataSourceClassName(properties.getXaDataSourceClassName());
        xaDataSource.setMinPoolSize(properties.getMinPoolSize());
        xaDataSource.setMaxPoolSize(properties.getMaxPoolSize());
        xaDataSource.setBorrowConnectionTimeout(properties.getBorrowConnectionTimeout());
        xaDataSource.setTestQuery(properties.getTestQuery());
        xaDataSource.setMaintenanceInterval(properties.getMaintenanceInterval());
        xaDataSource.setMaxIdleTime(properties.getMaxIdleTime());
        return xaDataSource;
    }

    public static DataSource buildJndiDataSource(String dataSourceName,JndiDataSourceProperties properties) {
        JndiObjectFactoryBean jndiObjectFactoryBean=new  JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName(properties.getJndiname());
        jndiObjectFactoryBean.setProxyInterface(DataSource.class);
        jndiObjectFactoryBean.setLookupOnStartup(false);
        try {
            jndiObjectFactoryBean.afterPropertiesSet();
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return (DataSource) jndiObjectFactoryBean.getObject();
    }

}
