package com.starkindustries.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Tocar el atributo obliga a generar/cargar el token y que el repo lo ponga en cookie
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        // (No hace falta hacer nada con 'csrf', solo accederlo)
        filterChain.doFilter(request, response);
    }
}
