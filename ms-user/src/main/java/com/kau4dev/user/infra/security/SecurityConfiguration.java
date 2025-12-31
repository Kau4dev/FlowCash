package com.kau4dev.user.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desabilita proteção contra ataques CSRF (necessário para APIs REST)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API REST não guarda estado/sessão
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/users").permitAll() // Libera o cadastro de usuários
                        .requestMatchers("/h2-console/**").permitAll() // Caso use H2 (opcional)
                        .anyRequest().authenticated() // O resto precisa de autenticação (mas por enquanto só temos /users)
                )
                .build();
    }
}