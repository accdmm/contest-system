package com.contest.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import com.contest.admin.config.MinioProperties;

@SpringBootApplication
@ComponentScan(basePackages = "com.contest")
@MapperScan("com.contest.**.mapper")
@EnableConfigurationProperties(MinioProperties.class)
public class ContestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContestApplication.class, args);
    }
}
