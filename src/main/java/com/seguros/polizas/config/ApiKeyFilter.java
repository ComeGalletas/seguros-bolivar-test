package com.seguros.polizas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Exige el header {@code x-api-key} en todas las peticiones (excepto la consola H2). */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "x-api-key";

    private final byte[] apiKey;

    public ApiKeyFilter(PolizasProperties properties) {
        this.apiKey = properties.apiKey().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isValid(request.getHeader(HEADER))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"status":401,"title":"Unauthorized","detail":"Header x-api-key ausente o inválido"}""");
    }

    private boolean isValid(String provided) {
        return provided != null && MessageDigest.isEqual(apiKey, provided.getBytes(StandardCharsets.UTF_8));
    }
}
