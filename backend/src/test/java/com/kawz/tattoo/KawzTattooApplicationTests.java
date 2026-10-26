package com.kawz.tattoo;

import com.kawz.tattoo.api.dto.ApiModels.ClienteInput;
import com.kawz.tattoo.api.dto.ApiModels.ClienteResponse;
import com.kawz.tattoo.api.dto.ApiModels.MovimentacaoInput;
import com.kawz.tattoo.api.dto.ApiModels.ProjetoInput;
import com.kawz.tattoo.api.dto.ApiModels.ProjetoResponse;
import com.kawz.tattoo.api.dto.ApiModels.TatuadorCreate;
import com.kawz.tattoo.api.dto.ApiModels.TatuadorResponse;
import com.kawz.tattoo.api.web.Paginacao;
import com.kawz.tattoo.domain.service.ClienteService;
import com.kawz.tattoo.domain.service.MovimentacaoService;
import com.kawz.tattoo.domain.service.ProjetoService;
import com.kawz.tattoo.domain.service.TatuadorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class KawzTattooApplicationTests {
    @Autowired TatuadorService tatuadores;
    @Autowired ClienteService clientes;
    @Autowired ProjetoService projetos;
    @Autowired MovimentacaoService movimentacoes;

    @Test
    void cadastraClienteETatuadorEAbreProjeto() {
        TatuadorResponse tatuador = tatuadores.criar(
                new TatuadorCreate("Kawz", "kawz@example.com", "senha-segura"));
        ClienteResponse cliente = clientes.criar(new ClienteInput(
                "Ana", "11999999999", "123.456.789-00", "@ana", "Instagram",
                null, null, "LEAD"));

        ProjetoResponse projeto = projetos.criar(new ProjetoInput(
                tatuador.id(), cliente.id(), "Fechamento de braço", "ORCAMENTO",
                new BigDecimal("3000.00"), new BigDecimal("500.00"), 3,
                LocalDate.now().plusMonths(1)));

        // Sem movimentações: nada recebido, sinal não pago, saldo = valor total
        assertThat(projeto.saldo()).isEqualByComparingTo("3000");
        assertThat(projeto.recebido()).isEqualByComparingTo("0");
        assertThat(projeto.sinalPago()).isFalse();

        // Sinal pago via movimentação: saldo = quanto falta receber
        movimentacoes.criar(new MovimentacaoInput(projeto.id(), "ENTRADA", "sinal",
                LocalDate.now(), new BigDecimal("500.00"), "PIX", "pago"));

        ProjetoResponse depois = projetos.buscar(projeto.id());
        assertThat(depois.saldo()).isEqualByComparingTo("2500.00");
        assertThat(depois.recebido()).isEqualByComparingTo("500.00");
        assertThat(depois.sinalPago()).isTrue();

        var pagina = projetos.listar(Paginacao.de(0, 20));
        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getTotalElements()).isEqualTo(1);
    }
}
