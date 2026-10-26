package com.example.task_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.example.task_api.security.RestAuthenticationEntryPoint;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionConfig -> sessionConfig
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeConfig -> authorizeConfig
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/register",
                                "/auth/login")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptionConfig -> exceptionConfig
                        .authenticationEntryPoint(
                                restAuthenticationEntryPoint))
                .oauth2ResourceServer(resourceServerConfig -> resourceServerConfig
                        .authenticationEntryPoint(
                                restAuthenticationEntryPoint)
                        .jwt(Customizer.withDefaults()));

        return http.build();
    }
}