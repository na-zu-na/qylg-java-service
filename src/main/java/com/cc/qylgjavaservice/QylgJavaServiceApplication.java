package com.cc.qylgjavaservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.cc.qylgjavaservice.mapper")
@EnableScheduling
public class QylgJavaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QylgJavaServiceApplication.class, args);
    }

}
