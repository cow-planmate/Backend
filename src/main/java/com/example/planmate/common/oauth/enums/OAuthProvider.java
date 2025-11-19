package com.example.planmate.common.oauth.enums;

import java.util.Arrays;

public enum OAuthProvider {
    KAKAO, GOOGLE, NAVER;

    public static OAuthProvider fromPath(String provider) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 provider: " + provider));
    }
}
