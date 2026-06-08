package com.toilet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 文档配置。
 * <p>
 * 大厂实践：为所有 REST API 提供标准化的交互式文档，
 * 前端和后端团队基于 Swagger 进行契约式协作。
 * </p>
 * <p>
 * 访问地址：http://localhost:8083/swagger-ui/index.html
 * </p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("公厕管理系统 API")
                        .version("1.0.0")
                        .description("公厕管理系统后端接口文档。\n\n" +
                                "包含模块：公厕管理、用户管理、保洁排班、报修工单、设施台账、耗材管理、公众反馈、数据看板。")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@toilet-management.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
