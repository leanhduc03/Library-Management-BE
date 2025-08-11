package com.spring.LibraryManagement.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép cookies
        config.setAllowCredentials(true);
        
        // Nguồn được phép gọi API
        config.addAllowedOrigin("http://localhost:3000"); // Frontend React mặc định
        
        // Các header được phép
        config.addAllowedHeader("*");
        
        // Các phương thức HTTP được phép
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // Các header được expose cho client
        config.addExposedHeader("Authorization");
        
        // Thời gian cache preflight request
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}