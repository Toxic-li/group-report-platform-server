package com.groupreport.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 接口文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("集团统计报表平台 API")
                        .description("集团统计报表平台后端接口文档<br/>" +
                                "<b>技术栈：</b>Spring Boot 3.x + MyBatis Plus + Sa-Token + MySQL 8 + Redis<br/><br/>" +
                                "<b>核心功能：</b><br/>" +
                                "- 组织机构管理（树形结构）<br/>" +
                                "- 用户与权限系统（RBAC）<br/>" +
                                "- 动态报表模板系统（核心）<br/>" +
                                "- 填报系统（支持Univer前端对接）<br/>" +
                                "- 审核流程<br/>" +
                                "- 数据汇总与统计")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GroupReport Platform Team")
                                .email("admin@group.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/auth/**", "/org/**", "/role/**", "/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi designerApi() {
        return GroupedOpenApi.builder()
                .group("designer")
                .pathsToMatch("/report-designer/**")
                .build();
    }
}
