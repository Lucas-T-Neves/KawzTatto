-- Migração para bancos existentes.
-- O schema.sql já contém estas colunas para instalações novas.
-- Aplicar uma única vez no banco MySQL da aplicação.

ALTER TABLE anamnese
  ADD COLUMN documento_assinado_url VARCHAR(1000) NULL,
  ADD COLUMN documento_assinado_public_id VARCHAR(255) NULL,
  ADD COLUMN documento_assinado_nome VARCHAR(255) NULL,
  ADD COLUMN documento_assinado_mime VARCHAR(100) NULL,
  ADD COLUMN documento_assinado_bytes BIGINT NULL,
  ADD COLUMN documento_assinado_em TIMESTAMP NULL;
