package com.reliaquest.api.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableCaching
public class ApiConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}