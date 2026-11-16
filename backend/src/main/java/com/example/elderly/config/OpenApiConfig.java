package com.example.elderly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置（Knife4j / Swagger 中文说明）
 * 访问地址: http://localhost:8080/doc.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("智慧养老管理系统 API")
                .description("面向养老院/社区的智慧养老管理平台接口文档，涵盖老人档案、设备管理、告警预警、健康档案、专家康养咨询、康养日程、数据统计等模块。")
                .version("1.0.0")
                .contact(new Contact().name("智慧养老团队")));
    }
}
