package com.example.providercomparison.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Allow all requests without login:
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                // Disable CSRF if you want to allow POST requests without a token
                .csrf(csrf -> csrf.disable())
                // Optionally disable form login
                .formLogin(Customizer.withDefaults());

        // Return the built filter chain
        return http.build();
    }
}
