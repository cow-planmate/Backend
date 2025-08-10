package com.example.planmate.common.auth;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private long authTokenExpirationMs = 86_400_000;
    private long emailVerificationTokenExpirationMs = 600_000;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // 토큰 생성
    public String generateToken(int userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + authTokenExpirationMs); // 1일

        return Jwts.builder()
                .setSubject(Integer.toString(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateToken(String email, EmailVerificationPurpose purpose) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + emailVerificationTokenExpirationMs); // 10분, 인증코드용 짧은 만료시간

        return Jwts.builder()
                .setSubject(email)
                .claim("purpose", purpose.name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    // 토큰에서 정보 추출
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public EmailVerificationPurpose getPurpose(String token) {
        String purposeStr = Jwts.parserBuilder()
                .setSigningKey(key)
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
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 잘못된 토큰
            return false;
        }
    }
}

