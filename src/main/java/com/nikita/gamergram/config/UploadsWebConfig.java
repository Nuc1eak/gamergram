package com.nikita.gamergram.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Without a trailing slash, Spring treats the location as a file.
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location.replaceAll("/+$", "/"));
    }
}
