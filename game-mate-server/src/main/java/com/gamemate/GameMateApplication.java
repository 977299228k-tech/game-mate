package com.gamemate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@MapperScan("com.gamemate.mapper")
public class GameMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameMateApplication.class, args);
    }
}