package com.teste.apis.imagem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.teste.apis.exception.RecursoNaoEncontradoException;
import com.teste.apis.imagem.dto.ImagemResponseDTO;
import com.teste.apis.imagem.dto.ImagemUrlDTO;
import com.teste.apis.imagem.dto.TransformacaoParams;
import com.teste.apis.integration.cloudinary.CloudinaryClient;
import com.teste.apis.integration.cloudinary.dto.CloudinaryUploadResult;
import com.teste.apis.integration.cloudinary.dto.TransformacaoUrl;

/** Testes do fluxo de imagens com o client do Cloudinary mockado. */
@ExtendWith(MockitoExtension.class)
class ImagemServiceTest {

    /** JPEG valido (magic bytes FF D8 FF ...). */
    private static final byte[] JPEG = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 0x4A, 0x46,
            0x49, 0x46, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, (byte) 0xFF, (byte) 0xD9
    };

    /** PNG valido (89 50 4E 47 0D 0A 1A 0A ...). */
    private static final byte[] PNG = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0, 0, 0, 0x0D, 0x49, 0x48, 0x44, 0x52, 0, 0, 0, 1, 0, 0, 0, 1
    };

    private static final byte[] LIXO = new byte[] {
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
    };

    @Mock
    private ImagemRepository repository;

    @Mock
    private CloudinaryClient cloudinaryClient;

    private ImagemService service() {
        return new ImagemService(repository, cloudinaryClient);
    }

    private static MockMultipartFile arquivo(byte[] conteudo, String nome, String contentType) {
        return new MockMultipartFile("arquivo", nome, contentType, conteudo);
    }

    @Test
    @DisplayName("deve aceitar JPEG valido, enviar ao provedor e salvar o registro")
    void aceitaJpeg() throws Exception {
        // a pasta padrao e resolvida dentro do client: o service repassa o que recebeu
        when(cloudinaryClient.upload(any(byte[].class), any()))
                .thenReturn(new CloudinaryUploadResult("studio/foto", "https://cdn/foto.jpg",
                        "jpg", 20480L, 800, 600));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImagemResponseDTO resposta = service().enviar(arquivo(JPEG, "foto.jpg", "image/jpeg"), null);

        verify(cloudinaryClient).upload(any(byte[].class), isNull());
        assertThat(resposta.publicId()).isEqualTo("studio/foto");
        assertThat(resposta.url()).isEqualTo("https://cdn/foto.jpg");
    }

    @Test
    @DisplayName("deve aceitar PNG valido")
    void aceitaPng() throws Exception {
        when(cloudinaryClient.upload(any(byte[].class), any()))
                .thenReturn(new CloudinaryUploadResult("studio/p", "https://cdn/p.png", "png", 1024L, 10, 10));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(service().enviar(arquivo(PNG, "foto.png", "image/png"), null).publicId())
                .isEqualTo("studio/p");
    }

    @Test
    @DisplayName("deve rejeitar arquivo que nao e imagem, mesmo com content-type de imagem")
    void rejeitaConteudoInvalido() {
        MockMultipartFile falso = arquivo(LIXO, "falso.jpg", "image/jpeg");

        // tipo invalido e erro de requisicao (400), nao falha de integracao
        assertThatThrownBy(() -> service().enviar(falso, null))
                .isInstanceOf(com.teste.apis.exception.RegraNegocioException.class)
                .hasMessageContaining("Formato nao suportado");

        verify(cloudinaryClient, never()).upload(any(byte[].class), any());
    }

    @Test
    @DisplayName("deve rejeitar arquivo maior que 10 MB sem chamar o provedor")
    void rejeitaArquivoGrande() {
        byte[] grande = new byte[(int) (10L * 1024 * 1024 + 1024)];
        System.arraycopy(JPEG, 0, grande, 0, JPEG.length);

        assertThatThrownBy(() -> service().enviar(arquivo(grande, "grande.jpg", "image/jpeg"), null))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("10 MB");

        verify(cloudinaryClient, never()).upload(any(byte[].class), any());
    }

    @Test
    @DisplayName("deve rejeitar envio vazio")
    void rejeitaVazio() {
        MockMultipartFile vazio = arquivo(new byte[0], "vazio.jpg", "image/jpeg");

        assertThatThrownBy(() -> service().enviar(vazio, null))
                .isInstanceOf(com.teste.apis.exception.RegraNegocioException.class);

        verify(cloudinaryClient, never()).upload(any(byte[].class), any());
    }

    @Test
    @DisplayName("deve usar a pasta informada quando ela existir")
    void usaPastaInformada() {
        when(cloudinaryClient.upload(any(byte[].class), eq("clientes/2026")))
                .thenReturn(new CloudinaryUploadResult("clientes/2026/f", "https://cdn/f.jpg", "jpg", 10L, 5, 5));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service().enviar(arquivo(JPEG, "f.jpg", "image/jpeg"), "clientes/2026");

        ArgumentCaptor<String> pasta = ArgumentCaptor.forClass(String.class);
        verify(cloudinaryClient).upload(any(byte[].class), pasta.capture());
        assertThat(pasta.getValue()).isEqualTo("clientes/2026");
    }

    @Test
    @DisplayName("deve gerar a URL transformada a partir dos parametros informados")
    void geraUrlTransformada() {
        Imagem imagem = Imagem.de("studio/foto", "https://cdn/foto.jpg", "jpg", 100L, 800, 600,
                OffsetDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(imagem));
        when(cloudinaryClient.gerarUrl(eq("studio/foto"), any(TransformacaoUrl.class)))
                .thenReturn("https://cdn/studio/foto/w_300,h_200,c_fill,f_webp,q_auto,e_grayscale.jpg");

        ImagemUrlDTO url = service().gerarUrl(1L, new TransformacaoParams(
                "300", "200", "FILL", "auto", "webp", "grayscale"));

        assertThat(url.url()).contains("w_300").contains("c_fill").contains("f_webp").contains("e_grayscale");

        ArgumentCaptor<TransformacaoUrl> captor = ArgumentCaptor.forClass(TransformacaoUrl.class);
        verify(cloudinaryClient).gerarUrl(eq("studio/foto"), captor.capture());
        assertThat(captor.getValue().corte()).isEqualTo("fill");
        assertThat(captor.getValue().largura()).isEqualTo("300");
        assertThat(captor.getValue().formato()).isEqualTo("webp");
    }

    @Test
    @DisplayName("deve remover no provedor e no banco")
    void removeImagem() {
        Imagem imagem = Imagem.de("studio/foto", "https://cdn/foto.jpg", "jpg", 100L, 800, 600,
                OffsetDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(imagem));

        service().remover(1L);

        verify(cloudinaryClient).remover("studio/foto");
        verify(repository).delete(imagem);
    }

    @Test
    @DisplayName("nao deve remover no banco quando o provedor falha")
    void falhaAoRemover() {
        Imagem imagem = Imagem.de("studio/foto", "https://cdn/foto.jpg", "jpg", 100L, 800, 600,
                OffsetDateTime.now());
        when(repository.findById(1L)).thenReturn(Optional.of(imagem));
        org.mockito.Mockito.doThrow(new com.teste.apis.exception.IntegrationException(
                "Cloudinary", "falhou", 500, false, null)).when(cloudinaryClient).remover("studio/foto");

        assertThatThrownBy(() -> service().remover(1L))
                .isInstanceOf(com.teste.apis.exception.IntegrationException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("deve devolver 404 ao buscar imagem inexistente")
    void naoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("deve listar as imagens salvas")
    void lista() {
        Imagem a = Imagem.de("a", "https://cdn/a.jpg", "jpg", 1L, 1, 1, OffsetDateTime.now());
        Imagem b = Imagem.de("b", "https://cdn/b.jpg", "jpg", 1L, 1, 1, OffsetDateTime.now());
        when(repository.findAllByOrderByCriadoEmDesc()).thenReturn(List.of(a, b));

        assertThat(service().listar()).hasSize(2);
    }
}
