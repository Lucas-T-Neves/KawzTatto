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
  musica_favorita VARCHAR(120) NULL,
  comida_favorita VARCHAR(120) NULL,
  estilo_tatuagem_favorita VARCHAR(120) NULL,
  faixa_media_orcamento DECIMAL(10,2) NULL,
  faixa_orcamento VARCHAR(120) NULL,
  habitos VARCHAR(255) NULL,
  disponibilidade_horarios VARCHAR(255) NULL,
  observacoes_gerais TEXT NULL,
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
  logo_url VARCHAR(1000) NULL,
  logo_public_id VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tatuador_email (email)
);

-- ---------------------------------------------------------
-- 2. Tabela cliente, Anamnese (1:N) e Atividades
-- ---------------------------------------------------------
CREATE TABLE cliente (
  id INT NOT NULL AUTO_INCREMENT,
  -- Dono do cliente: base da autorizacao por proprietario (todo SELECT de
  -- cliente/follow-up/anamnese filtra por esta coluna).
  tatuador_id INT NOT NULL,
  nome VARCHAR(150) NOT NULL,
  telefone VARCHAR(20) NULL,
  cpf VARCHAR(20) NULL,
  instagram VARCHAR(100) NULL,
  canal_aquisicao VARCHAR(45) NULL,
  link_conversa VARCHAR(255) NULL,
  link_pasta VARCHAR(255) NULL,
  localizacao VARCHAR(120) NULL,
  tamanho VARCHAR(45) NULL,
  ideia TEXT NULL,
  status VARCHAR(45) NULL,
  data_criacao DATETIME NULL,
  preferencias_cliente_id INT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_cliente_preferencias (preferencias_cliente_id),
  KEY idx_cliente_tatuador (tatuador_id),
  CONSTRAINT fk_cliente_preferencias
    FOREIGN KEY (preferencias_cliente_id) REFERENCES preferencias_cliente (id),
  CONSTRAINT fk_cliente_tatuador
    FOREIGN KEY (tatuador_id) REFERENCES tatuador (id)
);

-- 1 cliente -> 1 ou muitas anamneses (FK na anamnese; correção do DER)
CREATE TABLE anamnese (
  id INT NOT NULL AUTO_INCREMENT,
  logo_url VARCHAR(1000) NULL,
  nome VARCHAR(150) NULL,
  data_nascimento DATE NULL,
  ocupacao VARCHAR(120) NULL,
  telefone VARCHAR(20) NULL,
  whatsapp BOOLEAN NULL,
  endereco VARCHAR(500) NULL,
  rg VARCHAR(30) NULL,
  cpf VARCHAR(20) NULL,
  saude_json LONGTEXT NULL,
  data_assinatura DATE NULL,
  assinatura_url VARCHAR(1000) NULL,
  procedimento_json LONGTEXT NULL,
  pdf_url VARCHAR(1000) NULL,
  transaction_ref VARCHAR(100) NULL,
  documento_assinado_url VARCHAR(1000) NULL,
  documento_assinado_public_id VARCHAR(255) NULL,
  documento_assinado_nome VARCHAR(255) NULL,
  documento_assinado_mime VARCHAR(100) NULL,
  documento_assinado_bytes BIGINT NULL,
  documento_assinado_em TIMESTAMP NULL,
  dados_saude VARCHAR(45) NULL,
  preparacao_tatuagem VARCHAR(45) NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_anamnese_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);

-- Colunas titulo..observacoes (agenda do frontend) sao ACRESCIMO ao DER: o DER define so
-- descricao, tipo e tatuador_id.
CREATE TABLE atividade (
  id INT NOT NULL AUTO_INCREMENT,
  projeto_id INT NULL,
  cliente_id INT NULL,
  descricao VARCHAR(45) NULL,
  tipo VARCHAR(45) NULL,
  status VARCHAR(45) NULL,
  titulo VARCHAR(120) NULL,
  data_atividade DATE NULL,
  hora_inicio TIME NULL,
  hora_termino TIME NULL,
  local VARCHAR(255) NULL,
  observacoes VARCHAR(2000) NULL,
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
  descricao TEXT NULL,
  status_projeto VARCHAR(45) NULL,
  valor_total DECIMAL(10,2) NULL,
  valor_sinal DECIMAL(10,2) NULL,
  quantidade_sessoes INT NULL,
  data_validade DATE NULL,
  caracteristicas_projeto_id INT NOT NULL,
  tatuador_id INT NOT NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_projeto_caracteristicas (caracteristicas_projeto_id),
  CONSTRAINT fk_projeto_caracteristicas
    FOREIGN KEY (caracteristicas_projeto_id) REFERENCES caracteristicas_projeto (id),
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
  evento_calendario_id VARCHAR(100) NULL,
  duracao_horas INT NULL,
  projeto_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sessao_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id)
);

CREATE TABLE agenda_sync_pendente (
  id INT NOT NULL AUTO_INCREMENT,
  sessao_id INT NOT NULL,
  operacao VARCHAR(30) NOT NULL,
  tentativas INT NOT NULL DEFAULT 0,
  ultima_tentativa TIMESTAMP NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_agenda_sync_sessao FOREIGN KEY (sessao_id) REFERENCES sessao (id)
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
  categoria VARCHAR(45) NULL,
  data_ocorrencia DATE NULL,
  valor_monetario DECIMAL(10,2) NULL,
  forma_pagamento VARCHAR(45) NULL,
  status_pagamento VARCHAR(45) NULL,
  projeto_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_movimentacao_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id)
);

CREATE TABLE orcamento (
  id INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(150) NULL,
  data_orcamento DATE NULL,
  status VARCHAR(45) NULL,
  valor_total DECIMAL(10,2) NULL,
  valor_sinal DECIMAL(10,2) NULL,
  quantidade_sessoes INT NULL,
  forma_pagamento VARCHAR(45) NULL,
  data_validade DATE NULL,
  proposta_aceita BOOLEAN NULL,
  projeto_id INT NOT NULL,
  cliente_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_orcamento_projeto
    FOREIGN KEY (projeto_id) REFERENCES projeto (id),
  CONSTRAINT fk_orcamento_cliente
    FOREIGN KEY (cliente_id) REFERENCES cliente (id)
);
