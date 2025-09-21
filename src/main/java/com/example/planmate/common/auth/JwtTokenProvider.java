package com.example.planmate.common.auth;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import com.example.planmate.domain.refreshToken.service.RefreshTokenStore;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private Key accessKey;
    private Key refreshKey;
    private final long accessTtlMillis = 14L * 24 * 60 * 60 * 1000;
    private final long refreshTtlMillis = 14L * 24 * 60 * 60 * 1000;
    private long emailVerificationTokenExpirationMs = 600_000;

    private final RefreshTokenStore refreshTokenStore;

    @Value("${jwt.access secret}")
    private String accessSecret;
    @Value("${jwt.refresh secret}")
    private String refreshSecret;

    @PostConstruct
    void init() {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    // 토큰 생성
    public String generateAccessToken(int userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTtlMillis);

        return Jwts.builder()
                .claim("typ", "access")
                .setSubject(Integer.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateRefreshToken(int userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTtlMillis);
        String token = Jwts.builder()
                .claim("typ", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
        refreshTokenStore.insertRefreshToken(token, userId);
        return token;
    }
    public String generateEmailToken(String email, EmailVerificationPurpose purpose) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + emailVerificationTokenExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("purpose", purpose.name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }


    // 토큰에서 정보 추출
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public EmailVerificationPurpose getPurpose(String token) {
        String purposeStr = Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("purpose", String.class);
        return purposeStr == null ? null : EmailVerificationPurpose.valueOf(purposeStr);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 잘못된 토큰
            return false;
        }
    }


}

