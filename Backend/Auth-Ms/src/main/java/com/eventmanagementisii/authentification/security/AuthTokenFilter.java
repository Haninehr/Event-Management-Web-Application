package com.eventmanagementisii.authentification.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eventmanagementisii.authentification.service.AuthService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;



//src : https://chatgpt.com/c/6942e327-eab4-832d-b2e6-68985ac0651e 
//job : Intercept every HTTP request, extract the JWT token, validate it, and authenticate the user if the token is valid.

    //cad : AuthTokenFilter: authenticates users using JWT on every request and stores the authentication in Spring Security’s context.


@Component
@Slf4j

//Why OncePerRequestFilter?
    //Ensures the filter runs once per HTTP request
    //Avoids duplicate authentication attempts
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String BEARER_HR = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtil.validateToken(jwt)) {
                final String username = jwtUtil.getUserFromToken(jwt);
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith(BEARER_HR)) {
            return headerAuth.substring(BEARER_HR.length());
        }
        return null;
    }
}