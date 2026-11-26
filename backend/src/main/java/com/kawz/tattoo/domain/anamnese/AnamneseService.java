package com.kawz.tattoo.domain.anamnese;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kawz.tattoo.api.anamnese.AnamneseMapper;
import com.kawz.tattoo.api.anamnese.dto.AnamneseInput;
import com.kawz.tattoo.api.anamnese.dto.AnamneseResponse;
import com.kawz.tattoo.domain.cliente.Cliente;
import com.kawz.tattoo.domain.cliente.ClienteRepository;
import com.kawz.tattoo.domain.exception.ResourceNotFoundException;
import com.kawz.tattoo.domain.exception.DocumentoAssinadoInvalidoException;
import com.kawz.tattoo.infrastructure.integration.ApiTemplateClient;
import com.kawz.tattoo.infrastructure.integration.CloudinaryClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;

@Service
public class AnamneseService {
    private final AnamneseRepository anamneses;
    private final ClienteRepository clientes;
    private final ObjectMapper json;
    private final ApiTemplateClient apiTemplate;
    private final CloudinaryClient cloudinary;

    public AnamneseService(AnamneseRepository anamneses, ClienteRepository clientes, ObjectMapper json,
                           ApiTemplateClient apiTemplate, CloudinaryClient cloudinary) {
        this.anamneses = anamneses;
        this.clientes = clientes;
        this.json = json;
        this.apiTemplate = apiTemplate;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public List<AnamneseResponse> listarPorCliente(Integer clienteId, Integer tatuadorId) {
        obterClienteDoTatuador(clienteId, tatuadorId);
        return anamneses.findByClienteIdAndCliente_Tatuador_IdOrderByIdDesc(clienteId, tatuadorId).stream()
                .map(value -> AnamneseMapper.toResponse(value, json)).toList();
    }

    @Transactional(readOnly = true)
    public AnamneseResponse buscar(Integer id, Integer tatuadorId) {
        return AnamneseMapper.toResponse(obterDoTatuador(id, tatuadorId), json);
    }

    @Transactional
    public AnamneseResponse criar(AnamneseInput input, Integer tatuadorId) {
        Cliente cliente = obterClienteDoTatuador(input.clienteId(), tatuadorId);
        return AnamneseMapper.toResponse(
                anamneses.save(AnamneseMapper.toEntity(input, cliente, json)), json);
    }

    @Transactional
    public void excluir(Integer id, Integer tatuadorId) {
        anamneses.delete(obterDoTatuador(id, tatuadorId));
    }

    @Transactional
    public AnamneseResponse gerarPdf(Integer id, Integer tatuadorId) {
        Anamnese value = obterDoTatuador(id, tatuadorId);
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("logo_url", value.getLogoUrl());
        dados.put("nome", value.getNome());
        dados.put("data_nascimento", value.getDataNascimento());
        dados.put("ocupacao", value.getOcupacao());
        dados.put("telefone", value.getTelefone());
        dados.put("whatsapp", value.getWhatsapp());
        dados.put("endereco", value.getEndereco());
        dados.put("rg", value.getRg());
        dados.put("cpf", value.getCpf());
        dados.put("saude", jsonMap(value.getSaudeJson()));
        dados.put("data_assinatura", value.getDataAssinatura());
        dados.put("assinatura_url", value.getAssinaturaUrl());
        dados.put("procedimento", jsonMap(value.getProcedimentoJson()));
        ApiTemplateClient.PdfGerado pdf = apiTemplate.gerarAnamnese(dados);
        value.registrarPdf(pdf.downloadUrl(), pdf.transactionRef());
        return AnamneseMapper.toResponse(value, json);
    }

    @Transactional
    public AnamneseResponse enviarDocumentoAssinado(Integer id, MultipartFile arquivo, Integer tatuadorId) {
        Anamnese value = obterDoTatuador(id, tatuadorId);
        if (arquivo == null || arquivo.isEmpty()) throw new DocumentoAssinadoInvalidoException("Envie um arquivo PDF.");
        if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(arquivo.getContentType())) {
            throw new DocumentoAssinadoInvalidoException("O documento assinado deve ser um PDF.");
        }
        if (arquivo.getSize() > 10L * 1024 * 1024) {
            throw new DocumentoAssinadoInvalidoException("O PDF assinado deve ter no máximo 10 MB.");
        }
        try {
            byte[] bytes = arquivo.getBytes();
            CloudinaryClient.Enviada enviado = cloudinary.enviarDocumentoAssinado(bytes, id);
            value.registrarDocumentoAssinado(enviado.url(), enviado.publicId(), arquivo.getOriginalFilename(),
                    arquivo.getContentType(), arquivo.getSize(), Instant.now());
            return AnamneseMapper.toResponse(value, json);
        } catch (IOException ex) {
            throw new DocumentoAssinadoInvalidoException("Não foi possível ler o PDF assinado.", ex);
        }
    }

    @Transactional
    public void excluirDocumentoAssinado(Integer id, Integer tatuadorId) {
        Anamnese value = obterDoTatuador(id, tatuadorId);
        String publicId = value.getDocumentoAssinadoPublicId();
        value.removerDocumentoAssinado();
        cloudinary.removerDocumento(publicId);
    }

    private Map<?, ?> jsonMap(String value) {
        if (value == null || value.isBlank()) return Map.of();
        try {
            return json.readValue(value, Map.class);
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private Cliente obterClienteDoTatuador(Integer id, Integer tatuadorId) {
        return clientes.findByIdAndTatuadorId(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    private Anamnese obterDoTatuador(Integer id, Integer tatuadorId) {
        return anamneses.findByIdAndCliente_Tatuador_Id(id, tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Anamnese", id));
    }
}
