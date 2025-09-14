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
    // 이미지가 저장된 로컬 디렉터리 (예: C:/data/images/)
    // application.yml 에 spring.img.url 로 지정. (기존 명칭 유지)
    @Value("${spring.img.url:C:/data/images/}")
    private String imgUrl;

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

        // 2) 이미지 디렉터리 매핑 (/images/**)
        int oneYearSeconds = (int) Duration.ofDays(365).getSeconds();
        String normalizedImg = ensureTrailingSlash(imgUrl);
        String imgLocation = toFileLocation(normalizedImg);
        if (!registry.hasMappingForPattern("/images/googleplace/**")) {
            registry.addResourceHandler("/images/googleplace/**")
                .addResourceLocations(imgLocation)
                .setCachePeriod(oneYearSeconds);
        }
    }

    private String ensureTrailingSlash(String path) {
        if (path == null || path.isBlank()) return null; // 안전 기본값
        return path.endsWith("/") || path.endsWith("\\") ? path : path + "/";
    }

    private String toFileLocation(String dir) {
        // 이미 file: 로 시작하면 그대로 사용
        if (dir.startsWith("file:")) {
            return dir;
        }
        // 절대 경로로 변환 후 file: URI 문자열 반환
        return Path.of(dir).toAbsolutePath().toUri().toString();
    }
}