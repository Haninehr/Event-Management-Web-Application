package com.eventmanagementisii.authentification.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
//handle unauthenticated access to protected endpoints

//in JWT authentication:
    //No login page
    //No redirects
    //Pure REST API
//so instead of redirecting to /login => we return 401  and let frontend handle it with error or redirect or .... !
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "401 Unauthorized");

    }
}

