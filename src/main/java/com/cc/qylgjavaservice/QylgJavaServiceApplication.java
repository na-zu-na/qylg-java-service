package com.cc.qylgjavaservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cc.qylgjavaservice.mapper")
public class QylgJavaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QylgJavaServiceApplication.class, args);
    }

}
