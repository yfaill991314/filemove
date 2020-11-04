package com.funi.filemove.appconfig;

import com.funi.fastdfs.upload.FastDfsFileUpload;
import com.funi.fastdfs.upload.FastDfsFileUploadImpl;
import org.csource.common.MyException;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * @ClassName AppConfig
 * @Description TODO
 * @Author Feng.Yang
 * @Date 2020/5/14 17:20
 * @Version 1.0
 */
@SpringBootConfiguration
public class AppConfig {
    @Bean
    public FastDfsFileUpload getFastDfsFileUpload(){
        try {
            ClassPathResource classPathResource = new ClassPathResource("fastDfsConfig.properties");
            System.out.println(classPathResource.getFile().getAbsolutePath());
            return new FastDfsFileUploadImpl(classPathResource.getFile().getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
