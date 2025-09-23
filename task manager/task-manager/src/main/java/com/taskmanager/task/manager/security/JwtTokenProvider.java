package com.taskmanager.task.manager.security;

import com.taskmanager.task.manager.model.CustomUserDetails;
import com.taskmanager.task.manager.model.User;
import com.taskmanager.task.manager.service.UserService;
import com.taskmanager.task.manager.util.AppConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Getter
@Component
public class JwtTokenProvider {

    @Autowired
    private UserService userService;


    public String createToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user=userService.findByEmailAddress(userDetails.getEmail());
        Claims claims = Jwts.claims().setSubject(user.getId());
        claims.put("role", user.getRole().getRole());
        claims.put("username", userDetails.getUsername());


        Date now = new Date();
        Date validity = new Date(now.getTime() + AppConfig.JWT_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, AppConfig.JWT_SECRET)
                .compact();
    }


    public String getUserId(String token) {
        return Jwts.parser().setSigningKey(AppConfig.JWT_SECRET).parseClaimsJws(token).getBody().getSubject();
    }

    public String createEmailToken(String email) {
        String userID=userService.findByEmailAddress(email).getId();
        Claims claims = Jwts.claims().setSubject(userID);
        claims.put("email", email);


        Date now = new Date();
        Date validity = new Date(now.getTime() + AppConfig.JWT_EMAIL_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, AppConfig.JWT_SECRET)
                .compact();
    }
    public String createRefreshToken(String userId) {
        String userID=userService.findByUserIdToken(userId).getId();
        Claims claims = Jwts.claims().setSubject(userID);
        claims.put("role", userService.getUserById(userId).get().getRole());
        claims.put("username", userService.getUserById(userId).get().getUsername());

        Date now = new Date();
        Date validity = new Date(now.getTime() + AppConfig.JWT_REFRESH_EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, AppConfig.JWT_SECRET)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(AppConfig.JWT_SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public byte[] getSecretKey() {
        return AppConfig.JWT_SECRET.getBytes();
    }

    public long getValidityInMilliseconds() {
        return AppConfig.JWT_EXPIRATION;
    }

}
