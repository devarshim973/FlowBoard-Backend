//package com.flowboard.notification_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
//        http.cors(cors -> cors.disable())
//                .httpBasic(Customizer.withDefaults())
//                .authorizeHttpRequests(request -> {
//                    request.anyRequest().permitAll();
//                });
//        return http.build();
//    }
//}
