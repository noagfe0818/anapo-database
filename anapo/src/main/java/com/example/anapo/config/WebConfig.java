package com.example.anapo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")             // 모든 주소 접속 허용
                .allowedOrigins("http://localhost:3000") // 프론트엔드 포트(3000) 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 모든 방식 허용
                .allowCredentials(true);
    }
}