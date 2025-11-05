package com.example.planmate.common.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class AccessLogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("startAt", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        long took = System.currentTimeMillis() - (long) req.getAttribute("startAt");
        log.info("userId={} {} {} {}ms status={}",
                MDC.get("userId"),
                req.getMethod(),
                req.getRequestURI(),
                took,
                res.getStatus());
    }
}