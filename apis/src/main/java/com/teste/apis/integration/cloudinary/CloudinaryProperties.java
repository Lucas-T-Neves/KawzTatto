package com.teste.apis.integration.cloudinary;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    private String cloudName;
    private String apiKey;
    private String apiSecret;
    private String pastaPadrao = "studio/";
    private long connectTimeoutMs = 3000;
    private long readTimeoutMs = 15000;
}
