package com.nikita.gamergram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
// Registers JwtProperties as typed config.
@ConfigurationPropertiesScan
public class GamergramApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamergramApplication.class, args);
    }
}
