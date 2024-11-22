// src/main/java/com/finalcall/authenticationservice/config/WebConfig.java

package com.finalcall.authenticationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!imageUploadDir.endsWith("/")) {
            imageUploadDir += "/";
        }
        registry.addResourceHandler("/itemimages/**")
                .addResourceLocations("file:" + imageUploadDir);
    }
}
