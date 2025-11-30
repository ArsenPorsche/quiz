//package org.example.quiz.security;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
//import org.springframework.security.oauth2.jwt.*;
//import org.springframework.stereotype.Component;
//
//
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class JwtUtil {
//
//    private final JwtEncoder jwtEncoder;
//    private final JwtDecoder jwtDecoder;
//
//    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
//        this.jwtEncoder = jwtEncoder;
//        this.jwtDecoder = jwtDecoder;
//    }
//
////    public String generateToken(UserDetails userDetails) {
////        Instant now = Instant.now();
////        Map<String, Object> claims = new HashMap<>();
////        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
////        if (authority != null && authority.startsWith("ROLE_")) {
////            authority = authority.substring("ROLE_".length());
////        }
////        claims.put("role", authority);
////        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
////                .issuer("quiz-app")
////                .issuedAt(now)
////                .expiresAt(now.plus(10, ChronoUnit.HOURS))
////                .subject(userDetails.getUsername())
////                .claims(c -> c.putAll(claims))
////                .build();
////
////        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
////
////        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet))
////                .getTokenValue();
////    }
//
//    public String generateToken(UserDetails userDetails) {
//        Instant now = Instant.now();
//
//        String role = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .filter(a -> a.startsWith("ROLE_"))
//                .map(a -> a.substring(5))
//                .findFirst()
//                .orElse("PLAYER");
//
//        JwtClaimsSet claims = JwtClaimsSet.builder()
//                .issuer("quiz-master")
//                .issuedAt(now)
//                .expiresAt(now.plus(7, ChronoUnit.DAYS))
//                .subject(userDetails.getUsername())
//                .claim("role", List.of(role))
//                .build();
//
//        // ВОТ ЭТА СТРОКА — КЛЮЧЕВАЯ!
//        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
//                .getTokenValue();
//    }
//
//    public String extractUsername(String token) {
//        return jwtDecoder.decode(token).getSubject();
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        try {
//            Jwt jwt = jwtDecoder.decode(token);
//            String username = jwt.getSubject();
//            return username.equals(userDetails.getUsername()) && jwt.getExpiresAt().isAfter(Instant.now());
//        } catch (Exception e) {
//            return false;
//        }
//    }
//}

package org.example.quiz.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JwtUtil {

    private final JwtEncoder jwtEncoder;

    public JwtUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("PLAYER");

        var now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("quiz-master")
                .issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .subject(userDetails.getUsername())
                .claim("role", List.of(role))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}