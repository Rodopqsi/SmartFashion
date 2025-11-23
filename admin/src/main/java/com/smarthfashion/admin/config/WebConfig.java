package com.smarthfashion.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir;
        if (!location.startsWith("file:")) {
            if (!location.endsWith("/")) location = location + "/";
            location = "file:" + location;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
