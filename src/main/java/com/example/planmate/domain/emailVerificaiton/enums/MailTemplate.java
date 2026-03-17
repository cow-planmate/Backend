package com.example.planmate.domain.emailVerificaiton.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailTemplate {
    PASSWORD_RESET("planMate 임시 비밀번호입니다.", "임시 비밀번호: %s");

    private final String subject;
    private final String bodyTemplate;

    public String formatBody(String... args) {
        return String.format(bodyTemplate, (Object[]) args);
    }
}