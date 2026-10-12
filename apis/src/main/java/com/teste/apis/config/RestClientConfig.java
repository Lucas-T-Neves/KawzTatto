package com.teste.apis.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.teste.apis.integration.apitemplate.ApiTemplateProperties;
import com.teste.apis.integration.nylas.NylasProperties;

/**
 * Beans de RestClient por provedor, cada um com seus timeouts.
 * O builder injetado aqui e o do Spring, customizado pelo Boot com os
 * conversores JSON. Os clients recebem o RestClient por construtor,
 * o que permite mockar com MockRestServiceServer nos testes.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient apitemplateRestClient(RestClient.Builder builder, ApiTemplateProperties props) {
        return builder
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory(props.getConnectTimeoutMs(), props.getReadTimeoutMs()))
                .build();
    }

    @Bean
    public RestClient nylasRestClient(RestClient.Builder builder, NylasProperties props) {
        return builder
                .baseUrl(props.getBaseUrl())
                .requestFactory(requestFactory(props.getConnectTimeoutMs(), props.getReadTimeoutMs()))
                .build();
    }

    private ClientHttpRequestFactory requestFactory(long connectTimeoutMs, long readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return factory;
    }
}
