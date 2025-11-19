package com.example.planmate.common.oauth.config;

import java.util.Map;

import com.example.planmate.common.oauth.enums.OAuthProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    /** 프론트에게 JWT 전달할 때 redirect 할 URL */
    private String frontendRedirectUri;

    /** provider: {kakao, google, naver} */
    private Map<String, Provider> provider;

    @Getter
    @Setter
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizeUri;
        private String tokenUri;
        private String userInfoUri;
        private String scope;
    }

    /** Enum 기반으로 Provider 정보 조회 */
    public Provider getProvider(OAuthProvider provider) {
        return this.provider.get(provider.name().toLowerCase());
    }
}
