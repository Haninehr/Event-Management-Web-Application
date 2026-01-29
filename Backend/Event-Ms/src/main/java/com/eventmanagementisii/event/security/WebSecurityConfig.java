package com.eventmanagementisii.event.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public GET endpoints
                .requestMatchers(HttpMethod.GET, "/events", "/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/events-media/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/media/**").permitAll()


                // H2 console (optional)
                .requestMatchers("/h2-console", "/h2-console/**").permitAll()

               
                .requestMatchers("/actuator/**").permitAll()
                 .requestMatchers("/actuator2/**").permitAll()
                .requestMatchers( "/Internal/**").permitAll()

                // Organizer-only (write) endpoints
                .requestMatchers(HttpMethod.POST, "/events/**").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ORGANIZER")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

       
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    //cors !
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


// static recource configure !

@Bean
public WebMvcConfigurer staticResourceConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addResourceHandlers( org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/events-media/**")
                    .addResourceLocations("file:uploads/events-media/");
        }
    };
}


}
