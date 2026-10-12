package com.teste.apis.integration.apitemplate;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "apitemplate")
public class ApiTemplateProperties {

    private String baseUrl;
    private String apiKey;
    private String templateId;
    private long connectTimeoutMs = 3000;
    private long readTimeoutMs = 30000;
}
