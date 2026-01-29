package com.eventmanagementisii.registration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                
                .requestMatchers("/Internal/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/Internal/*/registeredUsers").permitAll()
                
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/notifications/trigger/event-cancel").permitAll()
                
              
                .requestMatchers(HttpMethod.GET, "/registrations/*/status").authenticated()
                .requestMatchers(HttpMethod.GET, "/registrations/*/registered").authenticated()
               
                .requestMatchers(HttpMethod.DELETE, "/registrations/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/registrations/*/accept/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/registrations/*/refuse/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/registrations/*/accepted").authenticated()
           
                .requestMatchers(HttpMethod.GET, "/registrations/*/participants").permitAll()
                .requestMatchers(HttpMethod.GET, "/notifications/**").authenticated()
                
                .requestMatchers("/registrations/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/registrations/*/participantsCount").permitAll()
                .anyRequest().authenticated()
                
            )
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:4200");
    config.addAllowedHeader("*");
    config.addExposedHeader("*");
    config.addAllowedMethod("*");
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
}