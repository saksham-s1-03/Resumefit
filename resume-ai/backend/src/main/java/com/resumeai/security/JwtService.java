package com.resumeai.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiryMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiry-hours}") long hours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = hours * 3600_000L;
    }

    public String generate(Long userId, String email) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key)
                .compact();
    }

    /** Returns the user id, or null if the token is invalid or expired. */
    public Long parseUserId(String token) {
        try {
            String sub = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return Long.valueOf(sub);
        } catch (Exception e) {
            return null;
        }
    }
}
