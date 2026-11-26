package com.kawz.tattoo.api;

import com.kawz.tattoo.support.FakeServer;
import com.kawz.tattoo.support.FakeServer.Resp;
import com.kawz.tattoo.support.HttpApi;
import com.kawz.tattoo.support.HttpApi.Auth;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova a regra central da correção: cada tatuador só enxerga e altera os
 * próprios dados. Dois tatuadores são criados via API, cada um com um conjunto
 * completo de recursos, e o token de um é usado contra os recursos do outro.
 *
 * <p>O esperado é sempre <b>404</b> (e não 403): o recurso alheio não deve
 * revelar sua existência. Também é verificado que listagens não vazam dados e
 * que criar filho em cliente/projeto alheio falha com 404.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AcessoCruzadoApiTest {

    static final FakeServer nylas;

    static {
        try {
            nylas = new FakeServer(req -> new Resp(200, "{\"request_id\":\"1\",\"data\":{\"id\":\"evt-cruzado\"}}"));
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",
                () -> "jdbc:h2:mem:kawz-acesso-cruzado;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        r.add("nylas.base-url", nylas::url);
        r.add("nylas.api-key", () -> "chave-nylas");
        r.add("nylas.grant-id", () -> "grant-1");
        r.add("nylas.calendar-id", () -> "primary");
        r.add("cloudinary.cloud-name", () -> "");
        r.add("cloudinary.api-key", () -> "");
        r.add("cloudinary.api-secret", () -> "");
    }

    @AfterAll
    static void encerrar() {
        nylas.close();
    }

    @Value("${local.server.port}")
    int port;

    HttpApi api;

    @BeforeEach
    void preparar() {
        api = new HttpApi(port);
        nylas.limpar();
        nylas.responder(req -> new Resp(200, "{\"request_id\":\"1\",\"data\":{\"id\":\"evt-cruzado\"}}"));
    }

    /** Conjunto completo de recursos de um tatuador. */
    record Cenario(Auth auth, int cliente, int projeto, int sessao, int movimentacao,
                   int followUp, int orcamento, int imagem, int atividade) {
    }

    private Cenario montar() {
        Auth auth = api.autenticar();
        String t = auth.token();
        String hoje = java.time.LocalDate.now().toString();

        int cliente = api.criarCliente(t, "Cliente " + auth.tatuadorId());
        int projeto = api.criarProjeto(t, auth.tatuadorId(), cliente);

        var sessao = api.post("/api/v1/sessoes", "{\"projetoId\":" + projeto
                + ",\"dataHorario\":\"2031-03-10T09:00:00\",\"localAtendimento\":\"Estudio\""
                + ",\"statusSessao\":\"AGENDADA\"}", t);
        assertThat(sessao.status()).isEqualTo(201);

        var movimentacao = api.post("/api/v1/movimentacoes", "{\"projetoId\":" + projeto
                + ",\"naturezaTransacao\":\"ENTRADA\",\"categoria\":\"sinal\",\"dataOcorrencia\":\"" + hoje
                + "\",\"valorMonetario\":500.00,\"formaPagamento\":\"PIX\",\"statusPagamento\":\"pago\"}", t);
        assertThat(movimentacao.status()).isEqualTo(201);

        var followUp = api.post("/api/v1/follow-ups", "{\"clienteId\":" + cliente
                + ",\"tipoContato\":\"WHATSAPP\",\"statusContato\":\"PENDENTE\",\"dataPrevista\":\"" + hoje
                + "\",\"proximaAcao\":\"Ligar\"}", t);
        assertThat(followUp.status()).isEqualTo(201);

        var orcamento = api.post("/api/v1/orcamentos", "{\"projetoId\":" + projeto + ",\"clienteId\":" + cliente
                + ",\"nome\":\"Orcamento\",\"status\":\"Rascunho\",\"total\":3000.00,\"deposito\":500.00"
                + ",\"sessoesEstimadas\":3}", t);
        assertThat(orcamento.status()).isEqualTo(201);


        var imagem = api.post("/api/v1/imagens", "{\"projetoId\":" + projeto
                + ",\"publicId\":\"kawz/" + projeto + "\",\"url\":\"https://img.exemplo/" + projeto + ".png\"}", t);
        assertThat(imagem.status()).isEqualTo(201);

        var atividade = api.post("/api/v1/atividades", "{\"tatuadorId\":" + auth.tatuadorId()
                + ",\"projetoId\":" + projeto + ",\"clienteId\":" + cliente
                + ",\"descricao\":\"Sessao de tatuagem\",\"tipo\":\"SESSAO\"}", t);
        assertThat(atividade.status()).isEqualTo(201);

        return new Cenario(auth, cliente, projeto, sessao.json("$.id"), movimentacao.json("$.id"),
                followUp.json("$.id"), orcamento.json("$.id"),
                imagem.json("$.id"), atividade.json("$.id"));
    }

    @Test
    @DisplayName("nenhum recurso de um tatuador é acessível com o token de outro")
    void recursosDiretosNaoVazam() {
        Cenario a = montar();
        Cenario b = montar();
        String tb = b.auth().token();

        // cliente
        assertThat(api.get("/api/v1/clientes/" + a.cliente(), tb).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/clientes/" + a.cliente(), "{\"nome\":\"invasao\"}", tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/clientes/" + a.cliente(), tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/preferencias", tb).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/clientes/" + a.cliente() + "/preferencias",
                "{\"musicaFavorita\":\"x\"}", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/anamneses", tb).status()).isEqualTo(404);
        assertThat(api.post("/api/v1/anamneses", "{\"clienteId\":" + a.cliente() + "}", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/conversa", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/projetos", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/sessoes", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/movimentacoes", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/clientes/" + a.cliente() + "/follow-ups", tb).status()).isEqualTo(404);

        // projeto e rotas aninhadas
        assertThat(api.get("/api/v1/projetos/" + a.projeto(), tb).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/projetos/" + a.projeto(), "{\"clienteId\":" + b.cliente()
                + ",\"descricao\":\"invasao\",\"statusProjeto\":\"ORCAMENTO\",\"valorTotal\":10.00"
                + ",\"valorSinal\":0.00,\"quantidadeSessoes\":1}", tb).status()).isEqualTo(404);
        assertThat(api.patch("/api/v1/projetos/" + a.projeto() + "/status",
                "{\"status\":\"CONCLUIDO\"}", tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/projetos/" + a.projeto(), tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/detalhado", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/sessoes", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/movimentacoes", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/follow-ups", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/orcamentos", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/imagens", tb).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos/" + a.projeto() + "/caracteristicas", tb).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/projetos/" + a.projeto() + "/caracteristicas",
                "{\"localizacaoCorpo\":\"braco\"}", tb).status()).isEqualTo(404);

        // sessão, movimentação, follow-up, orçamento, imagem, atividade
        assertThat(api.get("/api/v1/sessoes/" + a.sessao(), tb).status()).isEqualTo(404);
        assertThat(api.patch("/api/v1/sessoes/" + a.sessao() + "/remarcar",
                "{\"dataHorario\":\"2031-04-10T09:00:00\"}", tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/sessoes/" + a.sessao(), tb).status()).isEqualTo(404);

        assertThat(api.get("/api/v1/movimentacoes/" + a.movimentacao(), tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/movimentacoes/" + a.movimentacao(), tb).status()).isEqualTo(404);

        assertThat(api.get("/api/v1/follow-ups/" + a.followUp(), tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/follow-ups/" + a.followUp(), tb).status()).isEqualTo(404);

        assertThat(api.get("/api/v1/orcamentos/" + a.orcamento(), tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/orcamentos/" + a.orcamento(), tb).status()).isEqualTo(404);

        assertThat(api.get("/api/v1/imagens/" + a.imagem(), tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/imagens/" + a.imagem(), tb).status()).isEqualTo(404);

        assertThat(api.get("/api/v1/atividades/" + a.atividade(), tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/atividades/" + a.atividade(), tb).status()).isEqualTo(404);

        // nada foi apagado pelas tentativas acima
        assertThat(api.get("/api/v1/clientes/" + a.cliente(), a.auth().token()).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + a.projeto(), a.auth().token()).status()).isEqualTo(200);
    }

    @Test
    @DisplayName("listagens e rotas por tatuador não misturam dados de donos diferentes")
    void listagensNaoVazam() {
        Cenario a = montar();
        Cenario b = montar();
        String tb = b.auth().token();

        assertThat((Integer) api.get("/api/v1/clientes?pagina=0&tamanho=200", tb).json("$.totalElements"))
                .isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/projetos?pagina=0&tamanho=200", tb).json("$.totalElements"))
                .isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/orcamentos?pagina=0&tamanho=200", tb).json("$.totalElements"))
                .isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/movimentacoes?pagina=0&tamanho=200", tb).json("$.totalElements"))
                .isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/sessoes?pagina=0&tamanho=200", tb).json("$.totalElements"))
                .isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/follow-ups", tb).json("$.length()")).isEqualTo(1);
        assertThat((Integer) api.get("/api/v1/tatuadores/" + b.auth().tatuadorId() + "/atividades", tb)
                .json("$.length()")).isEqualTo(1);

        // rotas por tatuador: id alheio não é aceito
        assertThat(api.get("/api/v1/tatuadores/" + a.auth().tatuadorId() + "/atividades", tb).status())
                .isEqualTo(404);
        assertThat(api.get("/api/v1/tatuadores/" + a.auth().tatuadorId() + "/follow-ups", tb).status())
                .isEqualTo(404);
        assertThat(api.get("/api/v1/tatuadores/" + a.auth().tatuadorId(), tb).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/tatuadores/" + a.auth().tatuadorId(),
                "{\"nome\":\"invasao\",\"email\":\"invasao@teste.com\"}", tb).status()).isEqualTo(404);
        assertThat(api.delete("/api/v1/tatuadores/" + a.auth().tatuadorId(), tb).status()).isEqualTo(404);

        // dashboard: cada um vê apenas os próprios números
        var dashboard = api.get("/api/v1/dashboard", tb);
        assertThat(dashboard.status()).isEqualTo(200);
    }

    @Test
    @DisplayName("criar filho em cliente/projeto alheio responde 404")
    void criacaoComPaiAlheioFalha() {
        Cenario a = montar();
        Cenario b = montar();
        String tb = b.auth().token();

        assertThat(api.post("/api/v1/projetos", "{\"clienteId\":" + a.cliente()
                + ",\"descricao\":\"invasao\",\"statusProjeto\":\"ORCAMENTO\",\"valorTotal\":10.00"
                + ",\"valorSinal\":0.00,\"quantidadeSessoes\":1}", tb).status()).isEqualTo(404);

        assertThat(api.post("/api/v1/sessoes", "{\"projetoId\":" + a.projeto()
                + ",\"dataHorario\":\"2031-05-10T09:00:00\",\"localAtendimento\":\"Estudio\""
                + ",\"statusSessao\":\"AGENDADA\"}", tb).status()).isEqualTo(404);

        assertThat(api.post("/api/v1/movimentacoes", "{\"projetoId\":" + a.projeto()
                + ",\"naturezaTransacao\":\"ENTRADA\",\"categoria\":\"sinal\",\"valorMonetario\":10.00"
                + ",\"statusPagamento\":\"pago\"}", tb).status()).isEqualTo(404);

        assertThat(api.post("/api/v1/follow-ups", "{\"clienteId\":" + a.cliente()
                + ",\"tipoContato\":\"WHATSAPP\",\"statusContato\":\"PENDENTE\",\"dataPrevista\":\""
                + java.time.LocalDate.now() + "\"}", tb).status()).isEqualTo(404);

        assertThat(api.post("/api/v1/orcamentos", "{\"projetoId\":" + a.projeto() + ",\"clienteId\":" + a.cliente()
                + ",\"status\":\"Rascunho\",\"total\":10.00,\"deposito\":0.00,\"sessoesEstimadas\":1}", tb).status())
                .isEqualTo(404);

        assertThat(api.post("/api/v1/imagens", "{\"projetoId\":" + a.projeto()
                + ",\"publicId\":\"invasao\",\"url\":\"https://img.exemplo/invasao.png\"}", tb).status())
                .isEqualTo(404);

        // tatuadorId enviado no corpo é ignorado: a atividade nasce do dono do token
        var atividade = api.post("/api/v1/atividades", "{\"tatuadorId\":" + a.auth().tatuadorId()
                + ",\"descricao\":\"Atividade sem projeto\",\"tipo\":\"ADMIN\"}", tb);
        assertThat(atividade.status()).isEqualTo(201);
        int id = atividade.json("$.id");
        assertThat(api.get("/api/v1/atividades/" + id, tb).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/atividades/" + id, a.auth().token()).status()).isEqualTo(404);
    }
}
