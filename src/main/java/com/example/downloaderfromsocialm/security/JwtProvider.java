package com.example.downloaderfromsocialm.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.secretkey}")
    private String secretKey;

    @Value("${jwt.token.ttl}")
    private String ttl;

    public String generateToken(String username){
      return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+Long.parseLong(ttl)))
                .signWith(SignatureAlgorithm.HS512,secretKey)
                .compact();
    }

    public String getUsernameFromToken(String token){
      return   Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (SignatureException signatureException) {
            System.err.println("Invalid JWT signature");
        } catch (Exception exception) {
            System.err.println("Nimadir xatolik bor!");
        }
        return false;
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody().getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
