package com.teste.apis.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.teste.apis.integration.cloudinary.CloudinaryProperties;

/** Bean do SDK do Cloudinary, construido a partir das propriedades. */
@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties props) {
        Map<String, Object> config = ObjectUtils.asMap(
                "cloud_name", props.getCloudName(),
                "api_key", props.getApiKey(),
                "api_secret", props.getApiSecret(),
                "secure", Boolean.TRUE,
                // o SDK tem um timeout unico, em segundos: usamos o de leitura,
                // que e o que de fato limita o upload
                "timeout", (int) (props.getReadTimeoutMs() / 1000));
        return new Cloudinary(config);
    }
}
