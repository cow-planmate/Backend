package com.example.planmate.common.config;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.planmate.common.log.AccessLogInterceptor;

@Configuration
public class CorsGlobalConfig implements WebMvcConfigurer {
    @Value("${LOG_PATH:logs}")
    private String logPath;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000",
                        "http://localhost:63771",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "https://www.planmate.site",
                        "https://planmate.site",
                        "https://frontendplanmate-8sh5ish0j-donghyeoks-projects-dd8e94be.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AccessLogInterceptor())
                .addPathPatterns("/**") // 모든 요청
                .excludePathPatterns("/static/**", "/favicon.ico");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1) 로그 디렉터리 매핑
        String logAbsolute = Path.of(logPath).toAbsolutePath().toString();
        String logLocation = Path.of(logAbsolute).toUri().toString();
        if (!registry.hasMappingForPattern("/logs/**")) {
            registry.addResourceHandler("/logs/**")
                    .addResourceLocations(logLocation);
        }
    }
}