package com.example.lostfound_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("accessTokenCookie",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("accessToken")
                                        .description("로그인 성공 시 발급되는 HttpOnly JWT 쿠키")))
                .info(new Info()
                        .title("LostFound API")
                        .description("학교생활 분실물 관리 및 공지 API 문서. 분실물 등록, 수정, 삭제는 accessToken HttpOnly Cookie 인증이 필요합니다.")
                        .version("v1"));
    }
}
