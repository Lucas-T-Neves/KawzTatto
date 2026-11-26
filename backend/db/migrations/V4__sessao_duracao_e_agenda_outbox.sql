ALTER TABLE sessao ADD COLUMN duracao_horas INT NULL;

CREATE TABLE agenda_sync_pendente (
  id INT NOT NULL AUTO_INCREMENT,
  sessao_id INT NOT NULL,
  operacao VARCHAR(30) NOT NULL,
  tentativas INT NOT NULL DEFAULT 0,
  ultima_tentativa TIMESTAMP NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_agenda_sync_sessao FOREIGN KEY (sessao_id) REFERENCES sessao (id)
);
