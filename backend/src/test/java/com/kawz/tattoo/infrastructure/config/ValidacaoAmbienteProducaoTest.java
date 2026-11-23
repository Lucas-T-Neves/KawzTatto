package com.kawz.tattoo.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O perfil {@code prod} precisa falhar com mensagem clara quando faltam as
 * variáveis de ambiente — antes de tentar abrir conexão com o banco.
 */
class ValidacaoAmbienteProducaoTest {

    private final ValidacaoAmbienteProducao.VariaveisObrigatorias validacao =
            new ValidacaoAmbienteProducao.VariaveisObrigatorias();

    private void validarCom(MockEnvironment ambiente) {
        validacao.setEnvironment(ambiente);
        validacao.postProcessBeanFactory(null);
    }

    @Test
    @DisplayName("passa quando todas as variáveis estão definidas")
    void aceitaAmbienteCompleto() {
        MockEnvironment ambiente = new MockEnvironment()
                .withProperty("DATABASE_URL", "jdbc:mysql://localhost:3306/kawz")
                .withProperty("DATABASE_USERNAME", "kawz")
                .withProperty("DATABASE_PASSWORD", "segredo")
                .withProperty("JWT_SECRET", "x".repeat(32));
        assertThatCode(() -> validarCom(ambiente)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("lista todas as variáveis ausentes na mensagem de erro")
    void listaVariaveisAusentes() {
        assertThatThrownBy(() -> validarCom(new MockEnvironment()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DATABASE_URL")
                .hasMessageContaining("DATABASE_USERNAME")
                .hasMessageContaining("DATABASE_PASSWORD")
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("trata valor em branco como ausente")
    void trataValorEmBrancoComoAusente() {
        MockEnvironment ambiente = new MockEnvironment()
                .withProperty("DATABASE_URL", "jdbc:mysql://localhost:3306/kawz")
                .withProperty("DATABASE_USERNAME", "  ")
                .withProperty("DATABASE_PASSWORD", "segredo")
                .withProperty("JWT_SECRET", "x".repeat(32));
        assertThatThrownBy(() -> validarCom(ambiente))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("obrigatórias: DATABASE_USERNAME.");
    }
}
