-- Estrutura opcional para logo global do tatuador/estúdio.
-- Nenhum logo é definido por esta migração.

ALTER TABLE tatuador
  ADD COLUMN logo_url VARCHAR(1000) NULL,
  ADD COLUMN logo_public_id VARCHAR(255) NULL;
