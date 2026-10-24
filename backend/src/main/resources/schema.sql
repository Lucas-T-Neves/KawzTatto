-- View de contratos (vw_contratos) em H2.
-- Roda DEPOIS do DDL do Hibernate: spring.jpa.defer-datasource-initialization: true.
-- Adaptações H2 em relação ao script MySQL: CAST(... AS DATE) no lugar de DATE(...).
CREATE OR REPLACE VIEW vw_contratos AS
SELECT
  c.id                         AS contrato_id,
  cl.nome                      AS nome_cliente,
  cl.telefone                  AS telefone_cliente,
  t.nome                       AS nome_artista,
  p.descricao                  AS descricao_tatuagem,
  cp.localizacao_corpo         AS local_corpo,
  CAST((SELECT MIN(s.data_horario) FROM sessao s WHERE s.projeto_id = p.id) AS DATE) AS data_inicio,
  CAST((SELECT MAX(s.data_horario) FROM sessao s WHERE s.projeto_id = p.id) AS DATE) AS data_fim,
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
