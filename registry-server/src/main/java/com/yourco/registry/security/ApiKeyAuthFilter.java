package com.yourco.registry.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final SecurityConfig.SecurityProps props;

    public ApiKeyAuthFilter(SecurityConfig.SecurityProps props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!props.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(props.headerName());
        if (header == null || header.isBlank()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("ApiKey ")) header = auth.substring("ApiKey ".length());
        }

        if (header != null && !header.isBlank()) {
            List<GrantedAuthority> auths = new ArrayList<>();
            if (props.adminKeys().contains(header)) {
                auths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            if (!auths.isEmpty()) {
                Authentication auth = new UsernamePasswordAuthenticationToken("apikey", header, auths);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}

