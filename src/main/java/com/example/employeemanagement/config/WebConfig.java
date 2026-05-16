package com.example.employeemanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://employee-management-frontends.onrender.com",
            "https://employee-management-frontend.onrender.com");

    private final String allowedOrigins;

    public WebConfig(@Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,https://employee-management-frontends.onrender.com,https://employee-management-frontend.onrender.com}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(parseAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>(new CorsFilter(source));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private List<String> parseAllowedOrigins() {
        List<String> configuredOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .map(this::normalizeOrigin)
                .filter(origin -> !origin.isBlank())
                .toList();

        return java.util.stream.Stream.concat(DEFAULT_ALLOWED_ORIGINS.stream(), configuredOrigins.stream())
                .distinct()
                .toList();
    }

    private String normalizeOrigin(String origin) {
        return origin.replaceAll("/+$", "");
    }
}
