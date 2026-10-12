package com.teste.apis.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cloudinary.Cloudinary;
import com.teste.apis.integration.cloudinary.CloudinaryProperties;

/** O bean do Cloudinary e montado a partir das propriedades, sem valor no codigo. */
class CloudinaryConfigTest {

    @Test
    @DisplayName("deve aplicar credenciais e o timeout de leitura, em segundos, ao SDK")
    void montaConfig() {
        CloudinaryProperties props = new CloudinaryProperties();
        props.setCloudName("cloud-fake-de-teste");
        props.setApiKey("123456-fake");
        props.setApiSecret("segredo-fake-de-teste");
        props.setReadTimeoutMs(15000);

        Cloudinary cloudinary = new CloudinaryConfig().cloudinary(props);

        assertThat(cloudinary.config.cloudName).isEqualTo("cloud-fake-de-teste");
        assertThat(cloudinary.config.apiKey).isEqualTo("123456-fake");
        assertThat(cloudinary.config.apiSecret).isEqualTo("segredo-fake-de-teste");
        assertThat(cloudinary.config.secure).isTrue();
        assertThat(cloudinary.config.timeout).isEqualTo(15);
    }

    @Test
    @DisplayName("deve usar os defaults das propriedades quando nada e informado")
    void usaDefaults() {
        Cloudinary cloudinary = new CloudinaryConfig().cloudinary(new CloudinaryProperties());

        assertThat(cloudinary.config.timeout).isEqualTo(15);
    }
}
