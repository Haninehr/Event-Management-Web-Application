package com.eventmanagementisii.authentification.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

//job : creates, validates, and decodes JWT tokens using a 
        // secret key and is the core of stateless authentication in your application.
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    
    //token generate with 3 sections :  HEADER.PAYLOAD.SIGNATURE
    public String generateToken(String useremail,Long id, String role) {
        return Jwts.builder()
                .subject(String.valueOf(id))
                
                .claim("email", useremail)
              
                .claim("role", role) 
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public String getUserFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //get role 
    public String getRoleFromToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    //what this does ?
    //recompute the signature
    //authsignature=HMAC(jwtSecret, HEADER + PAYLOAD)

    // and than it verifie if authsignature == SIGNATURE (that came with HEADER and PAYLOAD )( the one that created during the login)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }
}