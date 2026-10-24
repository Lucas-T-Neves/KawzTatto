-- Script adaptado para o Segundo DER (DER-KawzTattoo.png)
-- MySQL 8+
--
-- Correção aplicada: anamnese é 1:N por cliente ("1 cliente tem uma ou muitas").
-- A tabela anamnese saiu da seção 1 e passou a ter cliente_id NOT NULL (FK),
-- e a coluna cliente.anamnese_id (com sua unique) foi removida.
-- Preferências do cliente permanece 1:1 (FK preferencias_cliente_id em cliente).

CREATE DATABASE IF NOT EXISTS kanz_tattoo;
USE kanz_tattoo;

-- ---------------------------------------------------------
-- 1. Tabelas Independentes / Sem Dependência Externa Directa
-- ---------------------------------------------------------
CREATE TABLE preferencias_cliente (
  id INT NOT NULL AUTO_INCREMENT,
  musica_favorita VARCHAR(45) NULL,
  comida_favorita VARCHAR(45) NULL,
  estilo_tatuagem_favorita VARCHAR(45) NULL,
  faixa_media_orcamento DECIMAL(10,2) NULL,
  habitos VARCHAR(45) NULL,
  disponibilidade_horarios VARCHAR(45) NULL,
  observacoes_gerais VARCHAR(45) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE contrato (
  id INT NOT NULL AUTO_INCREMENT,
  limite_alteracoes_arte VARCHAR(45) NULL,
  tolerancia_atraso VARCHAR(45) NULL,
  uso_imagem VARCHAR(45) NULL,
  data_assinatura DATE NULL,
  PRIMARY KEY (id)
);

CREATE TABLE caracteristicas_projeto (
  id INT NOT NULL AUTO_INCREMENT,
  ideia_expectativa TEXT NULL,
  estilo_arte VARCHAR(45) NULL,
  localizacao_corpo VARCHAR(45) NULL,
  dificuldade VARCHAR(45) NULL,
  cores VARCHAR(45) NULL,
  complexidade VARCHAR(45) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tatuador (
  id INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  senha VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tatuador_email (email)
);

-- ---------------------------------------------------------
-- 2. Tabela cliente, Anamnese (1:N) e Atividades
-- ---------------------------------------------------------
CREATE TABLE cliente (
  id INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(150) NOT NULL,
  telefone VARCHAR(20) NULL,
  instagram VARCHAR(100) NULL,
  canal_aquisicao VARCHAR(45) NULL,
  link_conversa VARCHAR(255) NULL,
  link_pasta VARCHAR(255) NULL,
  status VARCHAR(45) NULL,
  data_criacao DATETIME NULL,
  preferencias_cliente_id INT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_cliente_preferencias (preferencias_cliente_id),
  CONSTRAINT fk_cliente_preferencias
    FOREIGN KEY (preferencias_cliente_id) REFERENCES preferencias_cliente (id)
);

-- 1 cliente -> 1 ou muitas anamneses (FK na anamnese; correção do DER)
CREATE TABLE anamnese (
  id INT NOT NULL AUTO_INCREMENT,
  dados_saude VARCHAR(45) NULL,
  preparacao_tatuagem VARCHAR(45) NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_anamnese_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

CREATE TABLE atividade (
  id INT NOT NULL AUTO_INCREMENT,
  descricao VARCHAR(45) NULL,
  tipo VARCHAR(45) NULL,
  tatuador_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_atividade_tatuador
    FOREIGN KEY (tatuador_id) REFERENCES tatuador (id)
);

CREATE TABLE follow_up (
  id INT NOT NULL AUTO_INCREMENT,
  tipo_contato VARCHAR(45) NULL,
  status_contato VARCHAR(45) NULL,
  data_prevista DATE NULL,
  data_realizacao DATE NULL,
  descricao_observacao TEXT NULL,
  proxima_acao VARCHAR(45) NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_follow_up_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- ---------------------------------------------------------
-- 3. Tabela Central: projeto
-- ---------------------------------------------------------
CREATE TABLE projeto (
  id INT NOT NULL AUTO_INCREMENT,
  descricao VARCHAR(45) NULL,
  status_projeto VARCHAR(45) NULL,
  valor_total DECIMAL(10,2) NULL,
  valor_sinal DECIMAL(10,2) NULL,
  quantidade_sessoes INT NULL,
  data_validade DATE NULL,
  caracteristicas_projeto_id INT NOT NULL,
  contrato_id INT NOT NULL,
  tatuador_id INT NOT NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_projeto_contrato (contrato_id),
  UNIQUE KEY uq_projeto_caracteristicas (caracteristicas_projeto_id),
  CONSTRAINT fk_projeto_caracteristicas
    FOREIGN KEY (caracteristicas_projeto_id) REFERENCES caracteristicas_projeto (id),
  CONSTRAINT fk_projeto_contrato
    FOREIGN KEY (contrato_id) REFERENCES contrato (id),
  CONSTRAINT fk_projeto_tatuador
    FOREIGN KEY (tatuador_id) REFERENCES tatuador (id),
  CONSTRAINT fk_projeto_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- ---------------------------------------------------------
-- 4. Tabelas Dependentes do Projeto
-- ---------------------------------------------------------
CREATE TABLE sessao (
  id INT NOT NULL AUTO_INCREMENT,
  data_horario DATETIME NULL,
  local_atendimento VARCHAR(45) NULL,
  status_sessao VARCHAR(45) NULL,
  observacoes_sessao VARCHAR(45) NULL,
  projeto_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sessao_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id)
);

CREATE TABLE imagem (
  `imagem` INT NOT NULL AUTO_INCREMENT, -- Chave primária do DER
  publicId VARCHAR(255) NULL,
  url VARCHAR(1000) NULL,
  formato VARCHAR(20) NULL,
  bytes VARCHAR(100) NULL,
  `largura/altura` INT NULL,
  criadoEm DATETIME NULL,
  projeto_id INT NOT NULL,
  PRIMARY KEY (`imagem`),
  CONSTRAINT fk_imagem_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id) ON DELETE CASCADE
);

CREATE TABLE movimentacao_financeira (
  id INT NOT NULL AUTO_INCREMENT,
  natureza_transacao VARCHAR(45) NULL,
  categoria VARCHAR(80) NULL,
  data_ocorrencia DATE NULL,
  valor_monetario DECIMAL(10,2) NULL,
  forma_pagamento VARCHAR(45) NULL,
  status_pagamento VARCHAR(40) NULL,
  projeto_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_movimentacao_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id)
);

-- ---------------------------------------------------------
-- 5. View de Contratos (Ajustada para o DER novo)
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW vw_contratos AS
SELECT
  c.id                         AS contrato_id,
  cl.nome                      AS nome_cliente,
  cl.telefone                  AS telefone_cliente,
  t.nome                       AS nome_artista,
  p.descricao                  AS descricao_tatuagem,
  cp.localizacao_corpo         AS local_corpo,
  (SELECT DATE(MIN(s.data_horario)) FROM sessao s WHERE s.projeto_id = p.id) AS data_inicio,
  (SELECT DATE(MAX(s.data_horario)) FROM sessao s WHERE s.projeto_id = p.id) AS data_fim,
  p.valor_total                AS valor_orcado,
  EXISTS (
    SELECT 1 FROM movimentacao_financeira m
    WHERE m.projeto_id = p.id
      AND m.categoria = 'sinal'
      AND m.status_pagamento = 'pago'
  )                            AS sinal_pago,
  (SELECT m.forma_pagamento FROM movimentacao_financeira m
    WHERE m.projeto_id = p.id AND m.status_pagamento = 'pago'
    ORDER BY m.data_ocorrencia IS NULL, m.data_ocorrencia DESC, m.id DESC LIMIT 1) AS forma_pagamento,
  c.limite_alteracoes_arte,
  c.tolerancia_atraso,
  c.uso_imagem,
  c.data_assinatura
FROM contrato c
JOIN projeto p  ON p.contrato_id = c.id
JOIN tatuador t ON t.id = p.tatuador_id
JOIN cliente cl ON cl.id = p.cliente_id
LEFT JOIN caracteristicas_projeto cp ON cp.id = p.caracteristicas_projeto_id;
