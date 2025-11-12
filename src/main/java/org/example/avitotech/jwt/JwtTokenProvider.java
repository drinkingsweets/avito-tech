package org.example.avitotech.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            System.err.println("Invalid JWT signature: {}" + e);
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: {}" + e);
        } catch (ExpiredJwtException e) {
            System.err.println("Expired JWT token: {}" + e);
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT token: {}" + e);
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: {}" + e);
        }
        return false;
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.get("role");
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String createAdminToken(String userId) {
        return createToken(userId, "ADMIN");
    }

    public String createUserToken(String userId) {
        return createToken(userId, "USER");
    }

    private String createToken(String userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}

