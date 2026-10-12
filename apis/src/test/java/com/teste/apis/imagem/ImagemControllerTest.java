package com.teste.apis.imagem;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.teste.apis.exception.GlobalExceptionHandler;
import com.teste.apis.imagem.dto.ImagemResponseDTO;
import com.teste.apis.imagem.dto.ImagemUrlDTO;

/** Testes da camada web de imagens: status, validacao e formato das respostas. */
@WebMvcTest(ImagemController.class)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class ImagemControllerTest {

    private static final byte[] JPEG = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, (byte) 0xFF, (byte) 0xD9
    };

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImagemService service;

    @Test
    @DisplayName("POST /imagens com JPEG valido deve devolver 201 e publicId")
    void enviaComSucesso() throws Exception {
        when(service.enviar(any(), any()))
                .thenReturn(new ImagemResponseDTO(1L, "studio/foto", "https://cdn/foto.jpg",
                        "jpg", 20480L, 800, 600, OffsetDateTime.now()));

        mockMvc.perform(multipart("/imagens")
                        .file(new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg", JPEG)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.publicId").value("studio/foto"))
                .andExpect(jsonPath("$.url").value("https://cdn/foto.jpg"));
    }

    @Test
    @DisplayName("POST /imagens sem arquivo deve devolver 400")
    void exigeArquivo() throws Exception {
        mockMvc.perform(multipart("/imagens"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("POST /imagens com arquivo que nao e imagem deve devolver 400")
    void rejeitaConteudoInvalido() throws Exception {
        byte[] lixo = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
        };

        when(service.enviar(any(), any()))
                .thenThrow(new IllegalArgumentException(
                        "Formato nao suportado (falso.jpg): use JPEG, PNG ou WebP"));

        mockMvc.perform(multipart("/imagens")
                        .file(new MockMultipartFile("arquivo", "falso.jpg", "image/jpeg", lixo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /imagens/{id}/url com parametro invalido deve devolver 400")
    void rejeitaParametroInvalido() throws Exception {
        mockMvc.perform(get("/imagens/1/url")
                        .param("corte", "qualquerCoisa"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /imagens/{id}/url com largura fora da faixa deve devolver 400")
    void rejeitaLarguraInvalida() throws Exception {
        mockMvc.perform(get("/imagens/1/url")
                        .param("largura", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /imagens/{id}/url com qualidade invalida deve devolver 400")
    void rejeitaQualidadeInvalida() throws Exception {
        mockMvc.perform(get("/imagens/1/url")
                        .param("qualidade", "muitoAlta"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("GET /imagens/{id}/url com parametros validos deve devolver a URL")
    void geraUrl() throws Exception {
        when(service.gerarUrl(any(), any())).thenReturn(new ImagemUrlDTO("https://cdn/studio/foto/w_300.jpg"));

        mockMvc.perform(get("/imagens/1/url")
                        .param("largura", "300")
                        .param("corte", "fill")
                        .param("formato", "webp")
                        .param("qualidade", "auto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://cdn/studio/foto/w_300.jpg"));
    }

    @Test
    @DisplayName("DELETE /imagens/{id} deve devolver 204")
    void remove() throws Exception {
        mockMvc.perform(delete("/imagens/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /imagens/{id} inexistente deve devolver 404")
    void naoEncontrada() throws Exception {
        when(service.buscarPorId(99L))
                .thenThrow(new com.teste.apis.exception.RecursoNaoEncontradoException("Imagem nao encontrada: 99"));

        mockMvc.perform(get("/imagens/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
