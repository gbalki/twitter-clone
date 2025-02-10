package com.balki.twitter_clone.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileConfiguration {

    private String uploadPath;

    private String profileStorage = "profile";

    private String attachmentStorage = "attachments";

    public String getProfileStoragePath() {
        return uploadPath + "/" + profileStorage;
    }

    public String getAttachmentStoragePath() {
        return uploadPath + "/" + attachmentStorage;
    }
}
