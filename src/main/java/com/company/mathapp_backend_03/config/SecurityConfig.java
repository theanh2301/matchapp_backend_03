package com.company.mathapp_backend_03.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/subjects/overview/**").permitAll()
                    .requestMatchers("/api/chapters/**").permitAll()
                    .requestMatchers("/api/lessons/**").permitAll()
                    .requestMatchers("/api/flashcards/**").permitAll()
                    .requestMatchers("/api/quiz/**").permitAll()
                    .requestMatchers("/api/match_cards/**").permitAll()
                    .requestMatchers("/api/practices/**").permitAll()
                    .anyRequest().authenticated()
            );

        return http.build();
    }
}