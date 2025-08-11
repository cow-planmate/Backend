package com.example.planmate.common.config;

import com.example.planmate.common.log.AccessLogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

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
                        "https://www.planmate.site",
                        "https://planmate.site",
                        "https://frontendplanmate-8sh5ish0j-donghyeoks-projects-dd8e94be.vercel.app/")
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
        // logPath가 상대경로여도 절대경로로 변환해서 file: URI로 등록
        String absolute = Path.of(logPath).toAbsolutePath().toString();
        // OS별로 file: 접두어가 달라지지 않도록 URI 사용
        String location = Path.of(absolute).toUri().toString(); // ex) file:///C:/... or file:/var/...

        registry.addResourceHandler("/logs/**")
                .addResourceLocations(location); // 끝에 슬래시 없어도 URI가 폴더로 인식됨
    }
}