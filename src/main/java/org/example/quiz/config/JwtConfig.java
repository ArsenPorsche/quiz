//package org.example.quiz.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
//import org.springframework.security.oauth2.jwt.*;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//import java.util.Base64;
//
//@Configuration
//public class JwtConfig {
//
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//
//    @Bean
//    public JwtEncoder jwtEncoder() {
//        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
//        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
//
//        return new NimbusJwtEncoder(
//                new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secretKey.getEncoded())
//        );
//    }
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
//        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
//
//        return NimbusJwtDecoder.withSecretKey(secretKey)
//                .macAlgorithm(MacAlgorithm.HS256)
//                .build();
//    }
//}

package org.example.quiz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        var secret = new SecretKeySpec(keyBytes, "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(secret));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        var secret = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secret)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();
    }
}