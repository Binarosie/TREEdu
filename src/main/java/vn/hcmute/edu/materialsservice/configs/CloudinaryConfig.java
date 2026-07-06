package vn.hcmute.edu.materialsservice.configs;

import com.cloudinary.Cloudinary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        // Log để kiểm tra credentials đang load
        log.info("Cloudinary initialized with URL: {}",
                cloudinaryUrl != null ? cloudinaryUrl.replaceAll(":.*@", ":***@") : "NULL");

        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            log.error("cloudinary.url is missing in application.properties!");
            throw new IllegalArgumentException("Missing cloudinary.url configuration");
        }

        return new Cloudinary(cloudinaryUrl);
    }
}
