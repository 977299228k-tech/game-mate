package com.gamemate.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileUploadConfig {

    @Value("${game-mate.file.upload-path:./uploads/}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        Path dirPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dirPath);
            System.out.println("【FileUploadConfig】上传目录: " + dirPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("【FileUploadConfig】创建上传目录失败: " + e.getMessage());
        }
    }

    public String getUploadPath() {
        return uploadPath;
    }
}