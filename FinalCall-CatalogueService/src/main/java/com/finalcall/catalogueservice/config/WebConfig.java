// src/main/java/com/finalcall/catalogueservice/config/WebConfig.java

package com.finalcall.catalogueservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // For development; restrict in production
                .allowedMethods("*")
                .allowedHeaders("*");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure the path ends with a slash
        if (!imageUploadDir.endsWith("/")) {
            imageUploadDir += "/";
        }
        registry.addResourceHandler("/itemimages/**")
                .addResourceLocations("file:" + imageUploadDir);
    }
}
