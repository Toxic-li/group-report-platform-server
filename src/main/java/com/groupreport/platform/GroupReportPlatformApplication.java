package com.groupreport.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 集团统计报表平台 - 启动类
 * GroupReportPlatform Application
 *
 * 技术栈：
 * - Spring Boot 3.x
 * - MyBatis Plus
 * - Sa-Token (权限认证)
 * - MySQL 8
 * - Redis
 * - Knife4j (接口文档)
 */
@SpringBootApplication
@MapperScan("com.groupreport.platform.mapper")
@EnableCaching
@EnableAsync
public class GroupReportPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupReportPlatformApplication.class, args);
        System.out.println("========================================");
        System.out.println("  集团统计报表平台启动成功！");
        System.out.println("  接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("  Swagger文档: http://localhost:8080/api/swagger-ui.html");
        System.out.println("========================================");
    }
}
