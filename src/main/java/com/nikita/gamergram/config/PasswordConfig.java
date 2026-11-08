package com.nikita.gamergram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

// Separate from SecurityConfig: used by UserService as well as the login flow.
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Hashes are stored with an algorithm prefix, so the algorithm can change without breaking old passwords.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
