package com.kawz.tattoo.infrastructure.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * Falha rápido (e com mensagem útil) quando o perfil {@code prod} sobe sem as
 * variáveis de ambiente obrigatórias.
 *
 * <p>Sem isso o Spring tenta criar o {@code DataSource} com o placeholder
 * literal ({@code ${DATABASE_URL}}) e o erro final é apenas
 * {@code 'url' must start with "jdbc"}, que não diz o que faltou. Como
 * {@link BeanFactoryPostProcessor} roda antes da criação dos beans, a mensagem
 * aparece antes de qualquer tentativa de conexão.</p>
 */
@Configuration
@Profile("prod")
public class ValidacaoAmbienteProducao {

    /** Variáveis sem valor padrão no repositório. */
    static final List<String> OBRIGATORIAS = List.of(
            "DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD", "JWT_SECRET");

    @Bean
    static BeanFactoryPostProcessor variaveisDeAmbienteObrigatorias() {
        return new VariaveisObrigatorias();
    }

    static final class VariaveisObrigatorias implements BeanFactoryPostProcessor, EnvironmentAware {

        private Environment ambiente;

        @Override
        public void setEnvironment(Environment environment) {
            this.ambiente = environment;
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            List<String> faltando = OBRIGATORIAS.stream()
                    .filter(nome -> ausente(ambiente.getProperty(nome)))
                    .toList();
            if (!faltando.isEmpty()) {
                throw new IllegalStateException("""
                        Perfil 'prod' sem variáveis de ambiente obrigatórias: %s.
                        Defina-as antes de subir a aplicação (nenhuma delas tem valor padrão no repositório).
                        Exemplo: DATABASE_URL=jdbc:mysql://host:3306/kawz DATABASE_USERNAME=... \
                        DATABASE_PASSWORD=... JWT_SECRET=<segredo com pelo menos 32 bytes>"""
                        .formatted(String.join(", ", faltando)));
            }
        }

        private static boolean ausente(String valor) {
            return valor == null || valor.isBlank() || valor.startsWith("${");
        }
    }
}
