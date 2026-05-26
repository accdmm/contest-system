package com.contest.admin;

import com.contest.admin.config.MinioProperties;
import com.contest.ai.config.AiProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.contest")
@MapperScan("com.contest.**.mapper")
@EnableConfigurationProperties({MinioProperties.class, AiProperties.class})
public class ContestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContestApplication.class, args);
    }
}
