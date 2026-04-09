package com.smartpg.smartpg.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dno3xjmnq",
                "api_key", "412752235462317",
                "api_secret", "QSN8qbwg2cLTP1L53et0L44g54Q"
        ));
    }
}