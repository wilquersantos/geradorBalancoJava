package com.emissor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${app.security.token}")
    private String validToken;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // Permitir acesso livre ao endpoint de health check
        if (requestURI.equals("/api/health") || requestURI.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Validar token para endpoints da API
        if (requestURI.startsWith("/api/")) {
            String token = request.getHeader("X-API-Token");
            
            if (token == null || !token.equals(validToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Token inválido ou ausente\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
