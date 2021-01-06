package com.funi.filemove;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.funi.filemove.dao")
public class FilemoveApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilemoveApplication.class, args);
        String cmd = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe http://localhost:8088/filemove/index.html";
        Runtime run = Runtime.getRuntime();
        try{
            run.exec(cmd);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
