package org.example.quiz.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        if (authority != null && authority.startsWith("ROLE_")) {
            authority = authority.substring("ROLE_".length());
        }
        claims.put("role", authority);
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer("quiz-app")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.HOURS))
                .subject(userDetails.getUsername())
                .claims(c -> c.putAll(claims))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet))
                .getTokenValue();
    }

    public String extractUsername(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String username = jwt.getSubject();
            return username.equals(userDetails.getUsername()) && jwt.getExpiresAt().isAfter(Instant.now());
        } catch (Exception e) {
            return false;
        }
    }
}