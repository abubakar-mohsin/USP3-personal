package com.usp3.security.config;

import com.usp3.security.filter.ApiKeyAuthenticationFilter;
import com.usp3.security.service.ApiKeyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyService apiKeyService) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Typical for APIs
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // Everything must be checked
            )
            // Add your custom bouncer (Filter) before the default Spring check
            .addFilterBefore(new ApiKeyAuthenticationFilter(apiKeyService), 
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}