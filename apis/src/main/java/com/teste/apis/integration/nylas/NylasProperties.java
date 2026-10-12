package com.teste.apis.integration.nylas;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "nylas")
public class NylasProperties {

    private String baseUrl;
    private String apiKey;
    private String grantId;
    private String calendarId;
    private boolean notifyParticipantes = false;
    private long connectTimeoutMs = 3000;
    private long readTimeoutMs = 15000;
}
