package com.example.planmate.common.oauth.service;

import com.example.planmate.common.auth.JwtTokenProvider;
import com.example.planmate.common.oauth.config.OAuthProperties;
import com.example.planmate.common.oauth.dto.OAuthUserProfile;
import com.example.planmate.common.oauth.dto.google.GoogleTokenResponse;
import com.example.planmate.common.oauth.dto.google.GoogleUserResponse;
import com.example.planmate.common.oauth.dto.kakao.KakaoTokenResponse;
import com.example.planmate.common.oauth.dto.kakao.KakaoUserResponse;
import com.example.planmate.common.oauth.dto.naver.NaverTokenResponse;
import com.example.planmate.common.oauth.dto.naver.NaverUserResponse;
import com.example.planmate.common.oauth.enums.OAuthProvider;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final OAuthProperties oAuthProperties;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1) 로그인 시작 URL 생성
    public String buildAuthorizeUrl(OAuthProvider provider) {
        OAuthProperties.Provider config = oAuthProperties.getProvider(provider);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(config.getAuthorizeUri())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("response_type", "code");

        // 구글/네이버 스펙에 따라 추가 파라미터
        switch (provider) {
            case GOOGLE -> builder
                    .queryParam("access_type", "offline")
                    .queryParam("prompt", "consent");

            case NAVER -> builder
                    .queryParam("state", "RANDOM_STATE"); // 실서비스는 난수 사용
        }

        if (config.getScope() != null) {
            builder.queryParam("scope", config.getScope());
        }

        return builder.toUriString();
    }


    // 2) callback 처리: code → token → user → JWT 발급
    public String handleCallback(OAuthProvider provider, String code, String state) {

        // 1) provider별로 사용자 프로필 가져오기
        OAuthUserProfile profile = switch (provider) {
            case KAKAO -> fetchKakaoProfile(code);
            case GOOGLE -> fetchGoogleProfile(code);
            case NAVER -> fetchNaverProfile(code, state);
        };

        // 2) 유저 찾기 또는 생성
        User user = userService.findOrCreateOAuthUser(
                provider.name().toLowerCase(),
                profile.getProviderId(),
                profile.getEmail(),
                profile.getNickname()
        );

        // 3) JWT 발급
        String access = jwtTokenProvider.generateAccessToken(user.getUserId());
        String refresh = jwtTokenProvider.generateRefreshToken(user.getUserId());

        // 4) 최종 redirect URL 생성
        return buildFrontendRedirectUrl(access, refresh);
    }



    // 프론트 콜백 URL로 redirect
    private String buildFrontendRedirectUrl(String access, String refresh) {
        return UriComponentsBuilder.fromUriString(oAuthProperties.getFrontendRedirectUri())
                .queryParam("access", access)
                .queryParam("refresh", refresh)
                .build()
                .toString();
    }


    // ===================
    //   PROVIDER 구현부
    // ===================

    /** Kakao */
    private OAuthUserProfile fetchKakaoProfile(String code) {

        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.KAKAO);

        // 1) code → access token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", config.getClientId());
        form.add("client_secret", config.getClientSecret());
        form.add("redirect_uri", config.getRedirectUri());
        form.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(form, headers);

        KakaoTokenResponse tokenResponse = restTemplate.postForObject(
                config.getTokenUri(),
                tokenRequest,
                KakaoTokenResponse.class
        );

        String accessToken = tokenResponse.getAccessToken();

        // 2) access token → user info 요청
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(infoHeaders);

        ResponseEntity<KakaoUserResponse> userInfoResponse = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                userInfoRequest,
                KakaoUserResponse.class
        );

        KakaoUserResponse body = userInfoResponse.getBody();

        return new OAuthUserProfile(
                String.valueOf(body.getId()),
                body.getKakaoAccount().getEmail(),
                body.getKakaoAccount().getProfile().getNickname()
        );
    }


    /** Google */
    private OAuthUserProfile fetchGoogleProfile(String code) {

        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.GOOGLE);

        // 1) token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", config.getClientId());
        form.add("client_secret", config.getClientSecret());
        form.add("redirect_uri", config.getRedirectUri());
        form.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(form, headers);

        GoogleTokenResponse tokenResponse = restTemplate.postForObject(
                config.getTokenUri(),
                tokenRequest,
                GoogleTokenResponse.class
        );

        String accessToken = tokenResponse.getAccessToken();

        // 2) userinfo
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(infoHeaders);

        ResponseEntity<GoogleUserResponse> userInfoResponse = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                userInfoRequest,
                GoogleUserResponse.class
        );

        GoogleUserResponse body = userInfoResponse.getBody();

        return new OAuthUserProfile(
                body.getSub(),     // Google unique id
                body.getEmail(),
                body.getName()
        );
    }


    /** Naver */
    private OAuthUserProfile fetchNaverProfile(String code, String state) {

        OAuthProperties.Provider config = oAuthProperties.getProvider(OAuthProvider.NAVER);

        // 1) token 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", config.getClientId());
        form.add("client_secret", config.getClientSecret());
        form.add("redirect_uri", config.getRedirectUri());
        form.add("code", code);
        form.add("state", state);

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(form, headers);

        NaverTokenResponse tokenResponse = restTemplate.postForObject(
                config.getTokenUri(),
                tokenRequest,
                NaverTokenResponse.class
        );

        String accessToken = tokenResponse.getAccessToken();

        // 2) userinfo
        HttpHeaders infoHeaders = new HttpHeaders();
        infoHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> userInfoRequest = new HttpEntity<>(infoHeaders);

        ResponseEntity<NaverUserResponse> userInfoResponse = restTemplate.exchange(
                config.getUserInfoUri(),
                HttpMethod.GET,
                userInfoRequest,
                NaverUserResponse.class
        );

        NaverUserResponse.Result r = userInfoResponse.getBody().getResponse();

        return new OAuthUserProfile(
                r.getId(),
                r.getEmail(),
                r.getNickname()
        );
    }
}

