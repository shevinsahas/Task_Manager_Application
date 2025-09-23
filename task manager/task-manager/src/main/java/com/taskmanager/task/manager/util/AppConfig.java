package com.taskmanager.task.manager.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Value("${super.admin.username}")
    private String username;

    @Value("${super.admin.password}")
    private String password;

    @Value("${super.admin.email}")
    private String adminEmail;

    @Value("${base.url}")
    private String baseUrl;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.emailTokenExpiration}")
    public Long jwtEmailExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long jwtRefreshExpiration;


    public static String USERNAME;
    public static String PASSWORD;
    public static String BASE_URL;
    public static String EMAIL;
    public static String JWT_SECRET;
    public static Long JWT_EXPIRATION;
    public static Long JWT_EMAIL_EXPIRATION;
    public static Long JWT_REFRESH_EXPIRATION;

    public static final String ADMIN_FIRST_NAME="Super";
    public static final String ADMIN_LAST_NAME="Admin";


    @PostConstruct
    public void init() {
        USERNAME = username;
        PASSWORD = password;
        BASE_URL = baseUrl;
        EMAIL = adminEmail;
        JWT_SECRET = jwtSecret;
        JWT_EXPIRATION = jwtExpiration;
        JWT_EMAIL_EXPIRATION=jwtEmailExpiration;
        JWT_REFRESH_EXPIRATION = jwtRefreshExpiration;
    }




}

