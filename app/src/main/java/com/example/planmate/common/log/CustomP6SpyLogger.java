package com.example.planmate.common.log;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import com.p6spy.engine.spy.appender.Slf4JLogger;

public class CustomP6SpyLogger extends Slf4JLogger implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        String callerInfo = getCallerInfo();
        return String.format("[%s] %s | %d ms | %s | %s", now, callerInfo, elapsed, category, sql);
    }

    /**
     * StackTrace에서 실제 호출한 클래스와 메서드 이름 가져오기
     */
    private String getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.startsWith("com.example.planmate") && !className.contains("CustomP6SpyLogger")) {
                return className + "." + element.getMethodName();
            }
        }
        return "UnknownCaller";
    }
}
