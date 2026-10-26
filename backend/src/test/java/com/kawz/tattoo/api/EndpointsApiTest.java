package com.kawz.tattoo.api;

import com.kawz.tattoo.domain.exception.IntegracaoException;
import com.kawz.tattoo.integration.CloudinaryClient;
import com.kawz.tattoo.support.FakeServer;
import com.kawz.tattoo.support.FakeServer.Resp;
import com.kawz.tattoo.support.HttpApi;
import com.kawz.tattoo.support.HttpApi.Auth;
import com.kawz.tattoo.support.HttpApi.R;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Passa por TODOS os endpoints da API com HTTP real (porta aleatória, H2, JWT de verdade).
 * APITemplate e Nylas são servidores HTTP falsos locais; o Cloudinary é um mock do CloudinaryClient.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EndpointsApiTest {

    static final FakeServer apiTemplate;
    static final FakeServer nylas;

    static {
        try {
            apiTemplate = new FakeServer(req -> new Resp(200,
                    "{\"status\":\"success\",\"download_url\":\"https://pdf.exemplo/contrato.pdf\","
                            + "\"transaction_ref\":\"tx-42\"}"));
            nylas = new FakeServer(req -> new Resp(200, "{\"request_id\":\"1\",\"data\":{\"id\":\"evt-1\"}}"));
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry r) {
        // banco H2 próprio deste contexto (evita um contexto derrubar as tabelas do outro)
        r.add("spring.datasource.url", () -> "jdbc:h2:mem:kawz-endpoints;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        r.add("apitemplate.base-url", apiTemplate::url);
        r.add("apitemplate.api-key", () -> "chave-apitemplate");
        r.add("apitemplate.template-contrato-id", () -> "tpl-1");
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
        apiTemplate.close();
        nylas.close();
    }

    @MockitoBean CloudinaryClient cloudinary;
    @Value("${local.server.port}") int port;

    HttpApi api;
    Auth auth;
    String t; // token

    @BeforeEach
    void preparar() {
        api = new HttpApi(port);
        auth = api.autenticar();
        t = auth.token();
        apiTemplate.limpar();
        apiTemplate.responder(req -> new Resp(200,
                "{\"status\":\"success\",\"download_url\":\"https://pdf.exemplo/contrato.pdf\","
                        + "\"transaction_ref\":\"tx-42\"}"));
        nylas.limpar();
        nylas.responder(req -> new Resp(200, "{\"request_id\":\"1\",\"data\":{\"id\":\"evt-1\"}}"));
    }

    // ------------------------------------------------------------------ tatuadores / segurança

    @Test
    void tatuadoresEAutenticacao() {
        // rotas protegidas
        assertThat(api.get("/api/v1/clientes", null).status()).isEqualTo(401);
        assertThat(api.get("/api/v1/clientes", "token.invalido.aqui").status()).isEqualTo(401);

        // login com senha errada: 200 com autenticado=false
        R falha = api.post("/api/v1/tatuadores/autenticacao",
                "{\"email\":\"naoexiste@teste.com\",\"senha\":\"errada-123\"}", null);
        assertThat(falha.status()).isEqualTo(200);
        boolean autenticado = falha.json("$.autenticado");
        assertThat(autenticado).isFalse();

        // validação
        R invalido = api.post("/api/v1/tatuadores", "{\"nome\":\"\",\"email\":\"x\",\"senha\":\"1\"}", null);
        assertThat(invalido.status()).isEqualTo(400);

        // /me, listar, buscar
        R me = api.get("/api/v1/tatuadores/me", t);
        assertThat(me.status()).isEqualTo(200);
        int meuId = me.json("$.id");
        assertThat(meuId).isEqualTo(auth.tatuadorId());
        assertThat(api.get("/api/v1/tatuadores", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/tatuadores/" + auth.tatuadorId(), t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/tatuadores/9999999", t).status()).isEqualTo(404);

        // atualizar (senha opcional) e excluir
        String email = me.json("$.email");
        R atualizado = api.put("/api/v1/tatuadores/" + auth.tatuadorId(),
                "{\"nome\":\"Nome Novo\",\"email\":\"" + email + "\"}", t);
        assertThat(atualizado.status()).isEqualTo(200);
        String nome = atualizado.json("$.nome");
        assertThat(nome).isEqualTo("Nome Novo");
        assertThat(api.delete("/api/v1/tatuadores/" + auth.tatuadorId(), t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/tatuadores/" + auth.tatuadorId(), t).status()).isEqualTo(404);
    }

    @Test
    void atividadesDoTatuador() {
        R criada = api.post("/api/v1/atividades",
                "{\"tatuadorId\":" + auth.tatuadorId() + ",\"descricao\":\"Limpar maquinas\",\"tipo\":\"ROTINA\"}", t);
        assertThat(criada.status()).isEqualTo(201);
        int id = criada.json("$.id");

        assertThat(api.get("/api/v1/atividades/" + id, t).status()).isEqualTo(200);
        R lista = api.get("/api/v1/tatuadores/" + auth.tatuadorId() + "/atividades", t);
        assertThat(lista.status()).isEqualTo(200);
        int total = lista.json("$.length()");
        assertThat(total).isEqualTo(1);

        R atualizada = api.put("/api/v1/atividades/" + id,
                "{\"tatuadorId\":" + auth.tatuadorId() + ",\"descricao\":\"Esterilizar\",\"tipo\":\"ROTINA\"}", t);
        assertThat(atualizada.status()).isEqualTo(200);
        assertThat(api.post("/api/v1/atividades", "{\"tatuadorId\":" + auth.tatuadorId() + "}", t).status())
                .isEqualTo(400);

        assertThat(api.delete("/api/v1/atividades/" + id, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/atividades/" + id, t).status()).isEqualTo(404);
    }

    @Test
    void atividadeComAgendaCompleta() {
        String base = "\"tatuadorId\":" + auth.tatuadorId() + ",\"descricao\":\"Reuniao\",\"tipo\":\"AGENDA\"";
        R criada = api.post("/api/v1/atividades", "{" + base
                + ",\"titulo\":\"Alinhamento\",\"data\":\"2030-05-10\",\"inicio\":\"09:00\",\"termino\":\"10:30\""
                + ",\"local\":\"Estudio\",\"observacoes\":\"Levar projetos\"}", t);
        assertThat(criada.status()).isEqualTo(201);
        assertThat((String) criada.json("$.titulo")).isEqualTo("Alinhamento");
        assertThat((String) criada.json("$.local")).isEqualTo("Estudio");

        String url = "/api/v1/tatuadores/" + auth.tatuadorId() + "/atividades?data=";
        assertThat((int) api.get(url + "2030-05-10", t).json("$.length()")).isEqualTo(1);
        assertThat((int) api.get(url + "2030-05-11", t).json("$.length()")).isEqualTo(0);

        assertThat(api.post("/api/v1/atividades", "{" + base
                + ",\"data\":\"2030-05-10\",\"inicio\":\"10:00\",\"termino\":\"09:00\"}", t).status())
                .isEqualTo(422);
    }

    // ------------------------------------------------------------------ clientes e filhos

    @Test
    void clientesComPreferenciasAnamnesesEFollowUps() {
        // CRUD
        R criado = api.post("/api/v1/clientes",
                "{\"nome\":\"Ana\",\"telefone\":\"11999999999\",\"cpf\":\"111.222.333-44\",\"status\":\"LEAD\","
                        + "\"linkConversa\":\"https://wa.me/55119\"}", t);
        assertThat(criado.status()).isEqualTo(201);
        int id = criado.json("$.id");
        String cpfSalvo = criado.json("$.cpf");
        assertThat(cpfSalvo).isEqualTo("111.222.333-44");
        assertThat(api.post("/api/v1/clientes", "{\"nome\":\"\",\"status\":\"\"}", t).status()).isEqualTo(400);
        assertThat(api.get("/api/v1/clientes/" + id, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/clientes/9999999", t).status()).isEqualTo(404);
        R lista = api.get("/api/v1/clientes?pagina=0&tamanho=5", t);
        assertThat(lista.status()).isEqualTo(200);
        int total = lista.json("$.totalElements");
        assertThat(total).isGreaterThanOrEqualTo(1);
        R atualizado = api.put("/api/v1/clientes/" + id,
                "{\"nome\":\"Ana Maria\",\"status\":\"CLIENTE\",\"linkConversa\":\"https://wa.me/55119\"}", t);
        assertThat(atualizado.status()).isEqualTo(200);
        String nome = atualizado.json("$.nome");
        assertThat(nome).isEqualTo("Ana Maria");

        // /conversa
        R conversa = api.get("/api/v1/clientes/" + id + "/conversa", t);
        assertThat(conversa.status()).isEqualTo(200);
        String statusConversa = conversa.json("$.status");
        assertThat(statusConversa).isEqualTo("LINK_EXISTENTE");

        // preferências (1:1, criada junto do cliente)
        R pref = api.get("/api/v1/clientes/" + id + "/preferencias", t);
        assertThat(pref.status()).isEqualTo(200);
        R prefAtualizada = api.put("/api/v1/clientes/" + id + "/preferencias",
                "{\"musicaFavorita\":\"Rock\",\"faixaMediaOrcamento\":1500.00,\"habitos\":\"Noite\"}", t);
        assertThat(prefAtualizada.status()).isEqualTo(200);
        String musica = prefAtualizada.json("$.musicaFavorita");
        assertThat(musica).isEqualTo("Rock");

        // anamneses (1:N)
        R anamnese = api.post("/api/v1/anamneses",
                "{\"clienteId\":" + id + ",\"dadosSaude\":\"Sem alergias\",\"preparacaoTatuagem\":\"Jejum leve\"}", t);
        assertThat(anamnese.status()).isEqualTo(201);
        int anamneseId = anamnese.json("$.id");
        assertThat(api.get("/api/v1/anamneses/" + anamneseId, t).status()).isEqualTo(200);
        R anamneses = api.get("/api/v1/clientes/" + id + "/anamneses", t);
        assertThat(anamneses.status()).isEqualTo(200);
        int qtdAnamneses = anamneses.json("$.length()");
        assertThat(qtdAnamneses).isGreaterThanOrEqualTo(1);
        assertThat(api.put("/api/v1/anamneses/" + anamneseId,
                "{\"clienteId\":" + id + ",\"dadosSaude\":\"Asma\"}", t).status()).isEqualTo(200);
        assertThat(api.delete("/api/v1/anamneses/" + anamneseId, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/anamneses/" + anamneseId, t).status()).isEqualTo(404);

        // follow-ups
        String hoje = LocalDate.now().toString();
        R followUp = api.post("/api/v1/follow-ups", "{\"clienteId\":" + id + ",\"tipoContato\":\"WHATSAPP\","
                + "\"statusContato\":\"PENDENTE\",\"dataPrevista\":\"" + hoje + "\",\"proximaAcao\":\"Ligar\"}", t);
        assertThat(followUp.status()).isEqualTo(201);
        int followUpId = followUp.json("$.id");
        assertThat(api.get("/api/v1/follow-ups/" + followUpId, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/follow-ups", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/follow-ups?status=PENDENTE&dataAte=" + hoje, t).status()).isEqualTo(200);
        R porCliente = api.get("/api/v1/clientes/" + id + "/follow-ups", t);
        assertThat(porCliente.status()).isEqualTo(200);
        int qtdFollowUps = porCliente.json("$.length()");
        assertThat(qtdFollowUps).isEqualTo(1);
        assertThat(api.put("/api/v1/follow-ups/" + followUpId, "{\"clienteId\":" + id
                + ",\"tipoContato\":\"WHATSAPP\",\"statusContato\":\"FEITO\",\"dataPrevista\":\"" + hoje
                + "\",\"dataRealizacao\":\"" + hoje + "\"}", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/tatuadores/" + auth.tatuadorId() + "/follow-ups", t).status()).isEqualTo(200);
        assertThat(api.delete("/api/v1/follow-ups/" + followUpId, t).status()).isEqualTo(204);

        // exclusão
        assertThat(api.delete("/api/v1/clientes/" + id, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/clientes/" + id, t).status()).isEqualTo(404);
    }

    // ------------------------------------------------------------------ projetos

    @Test
    void projetosCaracteristicasEEscopos() {
        int clienteId = api.criarCliente(t, "Bruno");

        // regra de negócio: sinal maior que o total
        R regra = api.post("/api/v1/projetos", "{\"tatuadorId\":" + auth.tatuadorId() + ",\"clienteId\":" + clienteId
                + ",\"descricao\":\"X\",\"statusProjeto\":\"ORCAMENTO\",\"valorTotal\":100.00,\"valorSinal\":500.00,"
                + "\"quantidadeSessoes\":1}", t);
        assertThat(regra.status()).isEqualTo(422);
        assertThat(api.post("/api/v1/projetos", "{}", t).status()).isEqualTo(400);

        int id = api.criarProjeto(t, auth.tatuadorId(), clienteId);
        R buscado = api.get("/api/v1/projetos/" + id, t);
        assertThat(buscado.status()).isEqualTo(200);
        String saldo = String.valueOf((Object) buscado.json("$.saldo"));
        assertThat(new java.math.BigDecimal(saldo)).isEqualByComparingTo("3000");
        assertThat(api.get("/api/v1/projetos/9999999", t).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/projetos", t).status()).isEqualTo(200);

        assertThat(api.put("/api/v1/projetos/" + id, "{\"tatuadorId\":" + auth.tatuadorId() + ",\"clienteId\":"
                + clienteId + ",\"descricao\":\"Braco fechado\",\"statusProjeto\":\"ORCAMENTO\",\"valorTotal\":3500.00,"
                + "\"valorSinal\":500.00,\"quantidadeSessoes\":4}", t).status()).isEqualTo(200);
        R status = api.patch("/api/v1/projetos/" + id + "/status", "{\"status\":\"EM_ANDAMENTO\"}", t);
        assertThat(status.status()).isEqualTo(200);
        String novoStatus = status.json("$.statusProjeto");
        assertThat(novoStatus).isEqualTo("EM_ANDAMENTO");

        // características (1:1, criada junto do projeto)
        assertThat(api.get("/api/v1/projetos/" + id + "/caracteristicas", t).status()).isEqualTo(200);
        R car = api.put("/api/v1/projetos/" + id + "/caracteristicas",
                "{\"ideiaExpectativa\":\"Fechamento blackwork\",\"estiloArte\":\"Blackwork\",\"localizacaoCorpo\":\"Braco\"}", t);
        assertThat(car.status()).isEqualTo(200);
        String estilo = car.json("$.estiloArte");
        assertThat(estilo).isEqualTo("Blackwork");

        // escopos
        assertThat(api.get("/api/v1/projetos/" + id + "/contrato", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + id + "/sessoes", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + id + "/follow-ups", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + id + "/movimentacoes", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + id + "/imagens", t).status()).isEqualTo(200);
        R detalhado = api.get("/api/v1/projetos/" + id + "/detalhado", t);
        assertThat(detalhado.status()).isEqualTo(200);
        int idDetalhado = detalhado.json("$.projeto.id");
        assertThat(idDetalhado).isEqualTo(id);
        R doCliente = api.get("/api/v1/clientes/" + clienteId + "/projetos", t);
        assertThat(doCliente.status()).isEqualTo(200);
        int qtd = doCliente.json("$.length()");
        assertThat(qtd).isEqualTo(1);

        assertThat(api.delete("/api/v1/projetos/" + id, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/projetos/" + id, t).status()).isEqualTo(404);
    }

    // ------------------------------------------------------------------ sessões + Nylas

    @Test
    void sessoesSincronizamComOCalendarioNylas() {
        int clienteId = api.criarCliente(t, "Carla");
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), clienteId);
        String quando = "2030-01-15T14:00:00";

        // criar -> POST no Nylas
        R criada = api.post("/api/v1/sessoes", "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"" + quando
                + "\",\"localAtendimento\":\"Estudio\",\"statusSessao\":\"AGENDADA\"}", t);
        assertThat(criada.status()).isEqualTo(201);
        int id = criada.json("$.id");
        assertThat(nylas.requests).hasSize(1);
        var criarEvento = nylas.ultima();
        assertThat(criarEvento.method()).isEqualTo("POST");
        assertThat(criarEvento.uri()).isEqualTo("/v3/grants/grant-1/events?calendar_id=primary");
        assertThat(criarEvento.headers().get("authorization")).isEqualTo("Bearer chave-nylas");
        long epoch = LocalDateTime.parse(quando).atZone(ZoneId.of("America/Sao_Paulo")).toEpochSecond();
        assertThat(criarEvento.body()).contains("Carla").contains("\"start_time\":" + epoch);

        // remarcar -> PUT no mesmo evento
        R remarcada = api.patch("/api/v1/sessoes/" + id + "/remarcar", "{\"dataHorario\":\"2030-01-20T10:00:00\"}", t);
        assertThat(remarcada.status()).isEqualTo(200);
        String statusSessao = remarcada.json("$.statusSessao");
        assertThat(statusSessao).isEqualTo("REMARCADA");
        assertThat(nylas.ultima().method()).isEqualTo("PUT");
        assertThat(nylas.ultima().uri()).isEqualTo("/v3/grants/grant-1/events/evt-1?calendar_id=primary");

        // atualizar -> PUT
        assertThat(api.put("/api/v1/sessoes/" + id, "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"2030-01-21T10:00:00\""
                + ",\"localAtendimento\":\"Estudio 2\",\"statusSessao\":\"AGENDADA\"}", t).status()).isEqualTo(200);
        assertThat(nylas.ultima().method()).isEqualTo("PUT");

        // leituras e filtros
        assertThat(api.get("/api/v1/sessoes/" + id, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/sessoes/9999999", t).status()).isEqualTo(404);
        R porProjeto = api.get("/api/v1/sessoes?projetoId=" + projetoId, t);
        int qtdPorProjeto = porProjeto.json("$.totalElements");
        assertThat(qtdPorProjeto).isEqualTo(1);
        assertThat(api.get("/api/v1/sessoes?dataApos=2029-01-01", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/sessoes", t).status()).isEqualTo(200);
        R calendario = api.get("/api/v1/sessoes/calendario?inicio=2030-01-01&fim=2030-01-31", t);
        assertThat(calendario.status()).isEqualTo(200);
        int noMes = calendario.json("$.totalElements");
        assertThat(noMes).isGreaterThanOrEqualTo(1);
        assertThat(api.get("/api/v1/sessoes/calendario?inicio=2030-02-01&fim=2030-01-01", t).status()).isEqualTo(422);
        assertThat(api.get("/api/v1/clientes/" + clienteId + "/sessoes", t).status()).isEqualTo(200);
        assertThat(api.post("/api/v1/sessoes", "{\"projetoId\":" + projetoId + "}", t).status()).isEqualTo(400);

        // excluir -> DELETE no Nylas
        assertThat(api.delete("/api/v1/sessoes/" + id, t).status()).isEqualTo(204);
        assertThat(nylas.ultima().method()).isEqualTo("DELETE");
        assertThat(nylas.ultima().uri()).isEqualTo("/v3/grants/grant-1/events/evt-1?calendar_id=primary");
        assertThat(api.get("/api/v1/sessoes/" + id, t).status()).isEqualTo(404);
    }

    @Test
    void sessaoCanceladaSaiDoCalendario() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Marta"));
        R criada = api.post("/api/v1/sessoes", "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"2030-04-01T09:00:00\","
                + "\"localAtendimento\":\"Estudio\",\"statusSessao\":\"AGENDADA\"}", t);
        int id = criada.json("$.id");
        nylas.limpar();

        R cancelada = api.put("/api/v1/sessoes/" + id, "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"2030-04-01T09:00:00\","
                + "\"localAtendimento\":\"Estudio\",\"statusSessao\":\"CANCELADA\"}", t);

        assertThat(cancelada.status()).isEqualTo(200);
        assertThat(nylas.requests).hasSize(1);
        assertThat(nylas.ultima().method()).isEqualTo("DELETE");
        assertThat(nylas.ultima().uri()).isEqualTo("/v3/grants/grant-1/events/evt-1?calendar_id=primary");
    }

    @Test
    void falhaNoNylasDesfazACriacaoDaSessaoERetorna502() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Diego"));
        nylas.responder(req -> new Resp(500, "{}"));

        R r = api.post("/api/v1/sessoes", "{\"projetoId\":" + projetoId + ",\"dataHorario\":\"2030-03-01T09:00:00\","
                + "\"localAtendimento\":\"Estudio\",\"statusSessao\":\"AGENDADA\"}", t);

        assertThat(r.status()).isEqualTo(502);
        R sessoes = api.get("/api/v1/sessoes?projetoId=" + projetoId, t);
        int total = sessoes.json("$.totalElements");
        assertThat(total).isZero();
    }

    // ------------------------------------------------------------------ movimentações, dashboard

    @Test
    void movimentacoesResumoEDashboard() {
        int clienteId = api.criarCliente(t, "Elisa");
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), clienteId);
        String hoje = LocalDate.now().toString();
        String mes = YearMonth.now().toString();

        R sinal = api.post("/api/v1/movimentacoes", "{\"projetoId\":" + projetoId + ",\"naturezaTransacao\":\"ENTRADA\","
                + "\"categoria\":\"sinal\",\"dataOcorrencia\":\"" + hoje + "\",\"valorMonetario\":500.00,"
                + "\"formaPagamento\":\"PIX\",\"statusPagamento\":\"pago\"}", t);
        assertThat(sinal.status()).isEqualTo(201);
        int id = sinal.json("$.id");
        R despesa = api.post("/api/v1/movimentacoes", "{\"projetoId\":" + projetoId + ",\"naturezaTransacao\":\"SAIDA\","
                + "\"categoria\":\"material\",\"dataOcorrencia\":\"" + hoje + "\",\"valorMonetario\":100.00,"
                + "\"statusPagamento\":\"pendente\"}", t);
        assertThat(despesa.status()).isEqualTo(201);

        // validação (regex)
        assertThat(api.post("/api/v1/movimentacoes", "{\"projetoId\":" + projetoId + ",\"naturezaTransacao\":\"XYZ\","
                + "\"categoria\":\"a\",\"valorMonetario\":1,\"statusPagamento\":\"pago\"}", t).status()).isEqualTo(400);

        // o sinal pago reflete no projeto
        R projeto = api.get("/api/v1/projetos/" + projetoId, t);
        boolean sinalPago = projeto.json("$.sinalPago");
        assertThat(sinalPago).isTrue();
        assertThat(new java.math.BigDecimal(String.valueOf((Object) projeto.json("$.saldo")))).isEqualByComparingTo("2500");

        // leituras
        assertThat(api.get("/api/v1/movimentacoes/" + id, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/movimentacoes/9999999", t).status()).isEqualTo(404);
        assertThat(api.get("/api/v1/movimentacoes?mes=" + mes + "&status=pago&projetoId=" + projetoId, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/movimentacoes?clienteId=" + clienteId, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/movimentacoes?mes=invalido", t).status()).isEqualTo(422);
        assertThat(api.get("/api/v1/clientes/" + clienteId + "/movimentacoes", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/projetos/" + projetoId + "/movimentacoes/" + id, t).status()).isEqualTo(200);
        R resumo = api.get("/api/v1/movimentacoes/resumo?mes=" + mes, t);
        assertThat(resumo.status()).isEqualTo(200);
        assertThat(api.get("/api/v1/movimentacoes/resumo", t).status()).isEqualTo(400);
        assertThat(api.get("/api/v1/movimentacoes/resumo?mes=2020-13", t).status()).isEqualTo(422);
        assertThat(api.get("/api/v1/dashboard", t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/dashboard?mes=" + mes, t).status()).isEqualTo(200);

        // atualizar e excluir
        assertThat(api.put("/api/v1/movimentacoes/" + id, "{\"projetoId\":" + projetoId + ",\"naturezaTransacao\":\"ENTRADA\","
                + "\"categoria\":\"sinal\",\"dataOcorrencia\":\"" + hoje + "\",\"valorMonetario\":600.00,"
                + "\"formaPagamento\":\"PIX\",\"statusPagamento\":\"pago\"}", t).status()).isEqualTo(200);
        assertThat(api.delete("/api/v1/movimentacoes/" + id, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/movimentacoes/" + id, t).status()).isEqualTo(404);
    }

    // ------------------------------------------------------------------ imagens + Cloudinary

    @Test
    void imagensCrudManualEUploadParaOCloudinary() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Fabio"));

        // registro manual (sem upload)
        R manual = api.post("/api/v1/imagens", "{\"projetoId\":" + projetoId
                + ",\"publicId\":\"manual-1\",\"url\":\"https://img.exemplo/1.png\",\"formato\":\"png\"}", t);
        assertThat(manual.status()).isEqualTo(201);
        int manualId = manual.json("$.id");
        assertThat(api.post("/api/v1/imagens", "{\"projetoId\":" + projetoId
                + ",\"publicId\":\"manual-1\",\"url\":\"https://img.exemplo/1.png\"}", t).status()).isEqualTo(422);
        assertThat(api.get("/api/v1/imagens/" + manualId, t).status()).isEqualTo(200);
        assertThat(api.get("/api/v1/imagens/9999999", t).status()).isEqualTo(404);
        assertThat(api.put("/api/v1/imagens/" + manualId, "{\"projetoId\":" + projetoId
                + ",\"publicId\":\"manual-1\",\"url\":\"https://img.exemplo/2.png\",\"formato\":\"png\"}", t).status()).isEqualTo(200);

        // upload multipart -> Cloudinary (simulado)
        when(cloudinary.enviar(any(byte[].class), anyInt())).thenReturn(
                new CloudinaryClient.Enviada("kawz-tattoo/projeto-1/abc", "https://res.cloudinary.com/x/abc.png",
                        "png", 1234L, 800, 600));
        R enviada = api.upload("/api/v1/projetos/" + projetoId + "/imagens", t, "tattoo.png", "image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G'});
        assertThat(enviada.status()).isEqualTo(201);
        String publicId = enviada.json("$.publicId");
        String url = enviada.json("$.url");
        assertThat(publicId).isEqualTo("kawz-tattoo/projeto-1/abc");
        assertThat(url).isEqualTo("https://res.cloudinary.com/x/abc.png");
        int enviadaId = enviada.json("$.id");

        R lista = api.get("/api/v1/projetos/" + projetoId + "/imagens", t);
        int qtd = lista.json("$.length()");
        assertThat(qtd).isEqualTo(2);

        // arquivo que não é imagem e arquivo vazio
        assertThat(api.upload("/api/v1/projetos/" + projetoId + "/imagens", t, "a.txt", "text/plain",
                "oi".getBytes()).status()).isEqualTo(422);
        assertThat(api.upload("/api/v1/projetos/" + projetoId + "/imagens", t, "vazio.png", "image/png",
                new byte[0]).status()).isEqualTo(422);
        assertThat(api.upload("/api/v1/projetos/9999999/imagens", t, "x.png", "image/png",
                new byte[]{1}).status()).isEqualTo(404);
        assertThat(api.upload("/api/v1/projetos/" + projetoId + "/imagens", null, "x.png", "image/png",
                new byte[]{1}).status()).isEqualTo(401);

        // excluir remove do banco e do Cloudinary
        assertThat(api.delete("/api/v1/imagens/" + enviadaId, t).status()).isEqualTo(204);
        verify(cloudinary).remover("kawz-tattoo/projeto-1/abc");
        assertThat(api.delete("/api/v1/imagens/" + manualId, t).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/imagens/" + manualId, t).status()).isEqualTo(404);
    }

    @Test
    void excluirProjetoLimpaAsImagensNoCloudinary() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Nina"));
        api.post("/api/v1/imagens", "{\"projetoId\":" + projetoId
                + ",\"publicId\":\"limpar-1\",\"url\":\"https://img.exemplo/l.png\"}", t);

        assertThat(api.delete("/api/v1/projetos/" + projetoId, t).status()).isEqualTo(204);

        verify(cloudinary).remover("limpar-1");
    }

    @Test
    void falhaNoCloudinaryRetorna502() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Gabi"));
        when(cloudinary.enviar(any(byte[].class), anyInt())).thenThrow(new IntegracaoException("Cloudinary fora"));

        R r = api.upload("/api/v1/projetos/" + projetoId + "/imagens", t, "x.png", "image/png", new byte[]{1, 2});

        assertThat(r.status()).isEqualTo(502);
        R lista = api.get("/api/v1/projetos/" + projetoId + "/imagens", t);
        int qtd = lista.json("$.length()");
        assertThat(qtd).isZero();
    }

    // ------------------------------------------------------------------ contratos + APITemplate

    @Test
    void contratosEGeracaoDePdfNoApiTemplate() {
        int clienteId = api.criarCliente(t, "Helena");
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), clienteId);
        api.post("/api/v1/movimentacoes", "{\"projetoId\":" + projetoId + ",\"naturezaTransacao\":\"ENTRADA\","
                + "\"categoria\":\"sinal\",\"dataOcorrencia\":\"" + LocalDate.now() + "\",\"valorMonetario\":500.00,"
                + "\"formaPagamento\":\"PIX\",\"statusPagamento\":\"pago\"}", t);
        R doProjeto = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        assertThat(doProjeto.status()).isEqualTo(200);
        int contratoId = doProjeto.json("$.id");

        // preencher termos (criar), editar, assinar
        R criado = api.post("/api/v1/contratos", "{\"projetoId\":" + projetoId + ",\"limiteAlteracoesArte\":\"2\","
                + "\"toleranciaAtraso\":\"15 min\",\"usoImagem\":\"autorizado\"}", t);
        assertThat(criado.status()).isEqualTo(201);
        assertThat(api.put("/api/v1/contratos/" + contratoId,
                "{\"limiteAlteracoesArte\":\"3\",\"toleranciaAtraso\":\"20 min\",\"usoImagem\":\"autorizado\"}", t).status())
                .isEqualTo(200);
        R assinado = api.patch("/api/v1/contratos/" + contratoId + "/assinatura", "{\"data\":\"2030-01-10\"}", t);
        assertThat(assinado.status()).isEqualTo(200);
        String dataAssinatura = assinado.json("$.dataAssinatura");
        assertThat(dataAssinatura).isEqualTo("2030-01-10");
        assertThat(api.patch("/api/v1/contratos/" + contratoId + "/assinatura", null, t).status()).isEqualTo(200);

        // view vw_contratos
        R view = api.get("/api/v1/contratos/" + contratoId, t);
        assertThat(view.status()).isEqualTo(200);
        String cpfNaView = view.json("$.cpfCliente");
        assertThat(cpfNaView).isEqualTo("123.456.789-00");
        String statusNaView = view.json("$.statusContrato");
        assertThat(statusNaView).isEqualTo("ASSINADO");
        String nomeCliente = view.json("$.nomeCliente");
        boolean sinalPago = view.json("$.sinalPago");
        assertThat(nomeCliente).isEqualTo("Helena");
        assertThat(sinalPago).isTrue();
        R lista = api.get("/api/v1/contratos?pagina=0&tamanho=10", t);
        assertThat(lista.status()).isEqualTo(200);
        assertThat(api.get("/api/v1/contratos/9999999", t).status()).isEqualTo(404);

        // gerar PDF -> APITemplate
        R pdf = api.post("/api/v1/contratos/" + contratoId + "/pdf", "{\"tamanhoEstimado\":\"20cm\"}", t);
        assertThat(pdf.status()).isEqualTo(200);
        String pdfUrl = pdf.json("$.pdfUrl");
        String ref = pdf.json("$.transactionRef");
        assertThat(pdfUrl).isEqualTo("https://pdf.exemplo/contrato.pdf");
        assertThat(ref).isEqualTo("tx-42");
        var enviado = apiTemplate.ultima();
        assertThat(enviado.method()).isEqualTo("POST");
        assertThat(enviado.uri()).startsWith("/v2/create-pdf").contains("template_id=tpl-1");
        assertThat(enviado.headers().get("x-api-key")).isEqualTo("chave-apitemplate");
        assertThat(enviado.body())
                .contains("\"nome_cliente\":\"Helena\"")
                .contains("123.456.789-00") // CPF vem do cadastro do cliente
                .contains("20cm")
                .contains("\"sinal_pago\":\"Sim\"")
                .contains("\"forma_pagamento\":\"PIX\"")
                .contains("nome_artista");

        // persistiu no contrato
        R depois = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        String pdfSalvo = depois.json("$.pdfUrl");
        assertThat(pdfSalvo).isEqualTo("https://pdf.exemplo/contrato.pdf");

        // corpo é opcional
        assertThat(api.post("/api/v1/contratos/" + contratoId + "/pdf", null, t).status()).isEqualTo(200);
        assertThat(api.post("/api/v1/contratos/9999999/pdf", null, t).status()).isEqualTo(404);
    }

    @Test
    void statusDoContratoAcompanhaOFluxo() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Otavio"));
        R novo = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        int contratoId = novo.json("$.id");
        String inicial = novo.json("$.statusContrato");
        String criadoEm = novo.json("$.criadoEm");
        assertThat(inicial).isEqualTo("RASCUNHO");
        assertThat(criadoEm).isNotBlank();

        R gerado = api.post("/api/v1/contratos/" + contratoId + "/pdf", null, t);
        String aposPdf = gerado.json("$.statusContrato");
        assertThat(aposPdf).isEqualTo("GERADO");

        R assinado = api.patch("/api/v1/contratos/" + contratoId + "/assinatura", null, t);
        String aposAssinar = assinado.json("$.statusContrato");
        assertThat(aposAssinar).isEqualTo("ASSINADO");

        // gerar o PDF de novo depois de assinado não volta o status
        R denovo = api.post("/api/v1/contratos/" + contratoId + "/pdf", null, t);
        String final_ = denovo.json("$.statusContrato");
        assertThat(final_).isEqualTo("ASSINADO");
    }

    @Test
    void falhaNoApiTemplateRetorna502ENaoGravaPdf() {
        int projetoId = api.criarProjeto(t, auth.tatuadorId(), api.criarCliente(t, "Igor"));
        R contrato = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        int contratoId = contrato.json("$.id");
        apiTemplate.responder(req -> new Resp(500, "{\"status\":\"error\"}"));

        R r = api.post("/api/v1/contratos/" + contratoId + "/pdf", null, t);

        assertThat(r.status()).isEqualTo(502);
        R depois = api.get("/api/v1/projetos/" + projetoId + "/contrato", t);
        assertThat(depois.body()).doesNotContain("pdf.exemplo");
    }
}
