package sec.lab1.app.service;

import io.jsonwebtoken.security.Keys;
import sec.lab1.app.config.props.AppJwtProps;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Service
public class JwtService implements JwtDecoder {
    private final SecretKey key;
    private final long ttlMillis;

    public JwtService(AppJwtProps props) {
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = props.getTtlMinutes() * 60_000L;
    }

    public String issue(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMillis)))
                .signWith(key)
                .compact();
    }

    @Override
    public Jwt decode(String token) throws BadJwtException {
        try {
            var jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();

            Instant issuedAt  = claims.getIssuedAt()  != null ? claims.getIssuedAt().toInstant()  : null;
            Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : null;

            Map<String, Object> springHeaders = Map.of(
                "alg", "HS256",
                "typ", "JWT"
            );

            return new Jwt(token, issuedAt, expiresAt, springHeaders, claims);
        } catch (Exception e) {
            throw new BadJwtException("Invalid JWT: " + e.getMessage(), e);
        }
    }
}
