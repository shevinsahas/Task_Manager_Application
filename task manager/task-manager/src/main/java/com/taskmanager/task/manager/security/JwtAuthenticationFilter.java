package com.taskmanager.task.manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.task.manager.ResponseHandler.CustomException;
import com.taskmanager.task.manager.ResponseHandler.ErrorResponse;
import com.taskmanager.task.manager.model.Session;
import com.taskmanager.task.manager.model.User;
import com.taskmanager.task.manager.service.UserService;
import com.taskmanager.task.manager.util.Messages;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class JwtAuthenticationFilter implements Filter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Getter
    private String token;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            String token = getTokenFromRequest(request);
            setToken(token);


            String requestURI = request.getRequestURI();
            if(requestURI.startsWith("/uploads/")
                    || "/api/statistic".equals(requestURI)
                    || requestURI.startsWith("/api/menu-items")
                    || requestURI.startsWith("/api/cart")
                    || requestURI.startsWith("/api/admin/curries")
                    || requestURI.startsWith("/api/auth/")
                    || "/api/statistic/chart".equals(requestURI)
                    || requestURI.startsWith("/api/auth/login/")) {
                filterChain.doFilter(request, response);
                return;
            }

            if (token == null) {
                throw new CustomException(HttpStatus.UNAUTHORIZED.value(), Messages.YOU_ARE_NOT_AUTHORIZED, Messages.UNAUTHORIZED);
            }

            if (!jwtTokenProvider.validateToken(token)) {
                throw new CustomException(HttpStatus.UNAUTHORIZED.value(), Messages.YOU_ARE_NOT_AUTHORIZED, Messages.TOKEN_EXPIRED);
            }

            String userId = jwtTokenProvider.getUserId(token);

            var userDetails = userService.getUserById(userId);
            String role = userDetails.get().getRole();
            String httpMethod = request.getMethod();

            if (requestURI.startsWith("/api/auth/createUser")
                    || requestURI.startsWith("/api/team")
                    || requestURI.startsWith("/api/client")
                    || requestURI.startsWith("/api/projects")
            ) {
                if (("POST".equals(httpMethod) || "DELETE".equals(httpMethod) || "PUT".equals(httpMethod)) &&
                        (role == null || !role.equals("admin"))) {
                    throw new CustomException(HttpStatus.FORBIDDEN.value(), Messages.ACCESS_DENIED_MESSAGE, Messages.ACCESS_DENIED);
                }
            }

            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String newToken = jwtTokenProvider.createRefreshToken(userId);
            response.setHeader("Authorization", "Bearer " + newToken);



            filterChain.doFilter(request, response);
        } catch (CustomException ex) {
            setErrorResponse(response, ex);
        }
    }

    public void setToken(String token) {
        if (token != null) {
            this.token = token;
            Session.setUser(this.getAuthenticatedUser());
        }
    }

    private void setErrorResponse(HttpServletResponse response, CustomException ex) throws IOException {
        response.setStatus(ex.getStatus());
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(ex.getStatus(), ex.getMessage(), ex.getErrorId());
        ObjectMapper mapper = new ObjectMapper();
        String errorResponseString = mapper.writeValueAsString(errorResponse);
        response.getWriter().write(errorResponseString);
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
    public User getAuthenticatedUser() {
        if (token == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED.value(), Messages.INVALID_TOKEN, Messages.UNAUTHORIZED);
        }
        return userService.findUserByTokenHeader(token);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtTokenProvider.getSecretKey()).parseClaimsJws(token).getBody();
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long accessTokenValidityInMillis = jwtTokenProvider.getValidityInMilliseconds();

            // Check if the token's expiration time is within the access token validity period from the current time
            return expiration != null && expiration.getTime() <= now + accessTokenValidityInMillis;
        } catch (Exception e) {
            return false;
        }
    }
}
