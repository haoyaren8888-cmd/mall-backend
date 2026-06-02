package com.course.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.course.mall.mapper")
@SpringBootApplication
public class MallBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallBackendApplication.class, args);
    }
}
