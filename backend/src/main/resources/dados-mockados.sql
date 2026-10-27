-- =========================================================
-- Kawz Tattoo - Dados mockados para desenvolvimento (H2)
-- =========================================================
-- Objetivo: popular rapidamente o banco com dados ficticios e
-- coerentes para validar JWT, fluxos e telas da plataforma.
--
-- Login de demonstracao:
--   email: demo@kawz.local
--   senha: demo12345
-- =========================================================

-- ---------------------------
-- Limpeza (reexecucao segura)
-- ---------------------------
DELETE FROM atividade WHERE id BETWEEN 7951 AND 7999;
DELETE FROM follow_up WHERE id BETWEEN 7901 AND 7949;
DELETE FROM movimentacao_financeira WHERE id BETWEEN 7801 AND 7899;
DELETE FROM sessao WHERE id BETWEEN 7601 AND 7699;
DELETE FROM orcamento WHERE id BETWEEN 7701 AND 7799;
DELETE FROM projeto WHERE id BETWEEN 7501 AND 7599;
DELETE FROM anamnese WHERE id BETWEEN 7251 AND 7299;
DELETE FROM cliente WHERE id BETWEEN 7201 AND 7249;
DELETE FROM caracteristicas_projeto WHERE id BETWEEN 7401 AND 7449;
DELETE FROM contrato WHERE id BETWEEN 7301 AND 7349;
DELETE FROM preferencias_cliente WHERE id BETWEEN 7101 AND 7149;
DELETE FROM tatuador WHERE id = 7001 OR LOWER(email) = 'demo@kawz.local';

-- ---------------------------
-- Tatuador (login)
-- ---------------------------
-- senha plain text: demo12345
INSERT INTO tatuador (id, nome, email, senha)
VALUES (
    7001,
    'Artista Demo (FICTICIO)',
    'demo@kawz.local',
    'pbkdf2$210000$a2F3em1vY2tzZWVkMTIzNA==$KZ/x5fCpCZtarsOPI0Rzpd4sqYhZfsruuUa/iPt8SAQ='
);

-- ---------------------------
-- Preferencias + clientes
-- ---------------------------
INSERT INTO preferencias_cliente (
    id, musica_favorita, comida_favorita, estilo_tatuagem_favorita,
    faixa_media_orcamento, faixa_orcamento, habitos,
    disponibilidade_horarios, observacoes_gerais
) VALUES
    (7101, 'Rock', 'Hamburguer', 'Blackwork', 4200.00, 'R$ 3.000 a R$ 5.000', 'Treina 3x por semana', 'Sexta a tarde', 'Cliente ficticio para projeto em andamento'),
    (7102, 'Lo-fi', 'Acai', 'Minimalista', 1700.00, 'Ate R$ 2.000', 'Pele sensivel', 'Segunda de manha', 'Lead ficticio em fase de proposta'),
    (7103, 'Eletronica', 'Sushi', 'Oriental', 5600.00, 'Acima de R$ 5.000', 'Evitar exposicao solar', 'Sabado de manha', 'Projeto ficticio concluido'),
    (7104, 'MPB', 'Cafe', 'Fine line', 2300.00, 'R$ 2.000 a R$ 3.000', 'Horario comercial', 'Noite e sabado', 'Lead ficticio para acompanhamento comercial');

INSERT INTO cliente (
    id, nome, telefone, cpf, instagram, canal_aquisicao,
    link_conversa, link_pasta, localizacao, tamanho,
    ideia, status, data_criacao, preferencias_cliente_id
) VALUES
    (7201, 'Caio Freitas (FICTICIO)', '(11) 90000-1002', '000.000.000-02', '@caio.mock', 'Indicacao', 'https://wa.me/550000000002', 'https://drive.local/mock/caio', 'Osasco/SP', 'Grande', 'Fechamento blackwork braco esquerdo', 'CLIENTE', TIMESTAMP '2026-10-01 10:00:00', 7101),
    (7202, 'Marina Alves (FICTICIO)', '(11) 90000-1003', '000.000.000-03', '@marina.mock', 'Google', 'https://wa.me/550000000003', 'https://drive.local/mock/marina', 'Santo Andre/SP', 'Pequeno', 'Tatuagem minimalista no tornozelo', 'Interessado', TIMESTAMP '2026-10-07 09:30:00', 7102),
    (7203, 'Sofia Nunes (FICTICIO)', '(11) 90000-1005', '000.000.000-05', '@sofia.mock', 'Instagram', 'https://wa.me/550000000005', 'https://drive.local/mock/sofia', 'Sao Paulo/SP', 'Grande', 'Dragao oriental costas', 'CLIENTE', TIMESTAMP '2026-08-15 15:45:00', 7103),
    (7204, 'Laura Menezes (FICTICIO)', '(11) 90000-1001', '000.000.000-01', '@laura.mock', 'WhatsApp', 'https://wa.me/550000000001', 'https://drive.local/mock/laura', 'Sao Paulo/SP', 'Medio', 'Floral no antebraco', 'Interessado', TIMESTAMP '2026-10-10 14:20:00', 7104);

INSERT INTO anamnese (id, cliente_id, dados_saude, preparacao_tatuagem) VALUES
    (7251, 7201, 'Sem restricoes', 'Hidratar 48h antes'),
    (7252, 7202, 'Pele sensivel', 'Evitar cafeina no dia'),
    (7253, 7203, 'Sem alergias', 'Dormir bem na vespera'),
    (7254, 7203, 'Sensibilidade leve', 'Reforcar hidratacao'),
    (7255, 7204, 'Sem restricoes', 'Levar refeicao leve');

-- ---------------------------
-- Contratos + caracteristicas
-- ---------------------------
INSERT INTO contrato (
    id, limite_alteracoes_arte, tolerancia_atraso, uso_imagem,
    data_assinatura, status_contrato, criado_em, pdf_url, transaction_ref
) VALUES
    (7301, '2 alteracoes', '15 minutos', 'Autorizado', NULL, 'GERADO', TIMESTAMP '2026-10-03 10:10:00', 'https://files.local/mock/contrato-7301.pdf', 'txn-dev-7301'),
    (7302, '1 alteracao', '10 minutos', 'Somente com autorizacao', NULL, 'RASCUNHO', TIMESTAMP '2026-10-08 09:00:00', NULL, NULL),
    (7303, '3 alteracoes', '20 minutos', 'Autorizado', DATE '2026-07-20', 'ASSINADO', TIMESTAMP '2026-07-10 18:00:00', 'https://files.local/mock/contrato-7303.pdf', 'txn-dev-7303'),
    (7304, '2 alteracoes', '15 minutos', 'Negociavel', NULL, 'GERADO', TIMESTAMP '2026-10-11 11:35:00', 'https://files.local/mock/contrato-7304.pdf', 'txn-dev-7304');

INSERT INTO caracteristicas_projeto (
    id, ideia_expectativa, estilo_arte, localizacao_corpo,
    tamanho, dificuldade, cores, complexidade
) VALUES
    (7401, 'Cobertura com negativo', 'Blackwork', 'Braco esquerdo', 'Grande', 'Alta', 'Preto', 'Alta'),
    (7402, 'Linha fina com flor de lotus', 'Fine line', 'Tornozelo', 'Pequeno', 'Media', 'Preto e cinza', 'Media'),
    (7403, 'Dragao oriental completo', 'Oriental', 'Costas', 'Grande', 'Alta', 'Colorida', 'Alta'),
    (7404, 'Composicao floral delicada', 'Fine line', 'Antebraco', 'Medio', 'Media', 'Preto', 'Media');

-- ---------------------------
-- Projetos
-- ---------------------------
INSERT INTO projeto (
    id, tatuador_id, cliente_id, nome, descricao, link_pasta,
    status_projeto, valor_total, valor_sinal, quantidade_sessoes,
    data_validade, lembrete_pre, lembrete_post,
    contrato_id, caracteristicas_projeto_id
) VALUES
    (7501, 7001, 7201, 'Blackout Braco Esquerdo (FICTICIO)', 'Projeto ficticio blackwork em andamento', 'https://drive.local/mock/projeto-7501', 'EM_ANDAMENTO', 4200.00, 1200.00, 5, DATE '2026-12-20', 'Hidratar 7 dias antes', 'Nao expor ao sol por 30 dias', 7301, 7401),
    (7502, 7001, 7202, 'Minimalista Tornozelo (FICTICIO)', 'Projeto ficticio minimalista em orcamento', 'https://drive.local/mock/projeto-7502', 'ORCAMENTO', 1800.00, 500.00, 2, DATE '2026-11-15', 'Evitar alcool 24h antes', 'Pomada cicatrizante por 7 dias', 7302, 7402),
    (7503, 7001, 7203, 'Dragao Costas (FICTICIO)', 'Projeto ficticio concluido para historico', 'https://drive.local/mock/projeto-7503', 'CONCLUIDO', 5600.00, 1600.00, 4, DATE '2026-09-30', 'Reforcar alimentacao', 'Acompanhar cicatrizacao por 30 dias', 7303, 7403),
    (7504, 7001, 7204, 'Floral Antebraco (FICTICIO)', 'Projeto ficticio em fase de planejamento', 'https://drive.local/mock/projeto-7504', 'PLANEJAMENTO', 2600.00, 800.00, 3, DATE '2026-12-05', 'Dormir bem na vespera', 'Lavar 3x ao dia', 7304, 7404);

-- ---------------------------
-- Agenda de sessoes
-- ---------------------------
INSERT INTO sessao (
    id, projeto_id, data_horario, local_atendimento,
    status_sessao, observacoes_sessao, evento_calendario_id
) VALUES
    (7601, 7501, TIMESTAMP '2026-10-05 14:00:00', 'Estudio Centro', 'REALIZADA', 'Sessao 1', NULL),
    (7602, 7501, TIMESTAMP '2026-10-18 14:00:00', 'Estudio Centro', 'REALIZADA', 'Sessao 2', NULL),
    (7603, 7501, TIMESTAMP '2026-11-03 14:00:00', 'Estudio Centro', 'AGENDADA', 'Sessao 3', NULL),
    (7604, 7502, TIMESTAMP '2026-11-07 10:00:00', 'Estudio Centro', 'AGENDADA', 'Primeira sessao', NULL),
    (7605, 7502, TIMESTAMP '2026-10-28 10:00:00', 'Estudio Centro', 'CANCELADA', 'Cliente pediu novo horario', NULL),
    (7606, 7503, TIMESTAMP '2026-07-15 09:00:00', 'Estudio Jardins', 'REALIZADA', 'Sessao 1', NULL),
    (7607, 7503, TIMESTAMP '2026-08-02 09:00:00', 'Estudio Jardins', 'REALIZADA', 'Sessao 2', NULL),
    (7608, 7504, TIMESTAMP '2026-11-01 16:00:00', 'Estudio Jardins', 'REMARCADA', 'Ajuste por conflito de agenda', NULL);

-- ---------------------------
-- Orcamentos
-- ---------------------------
INSERT INTO orcamento (
    id, projeto_id, cliente_id, nome, data_orcamento, status,
    valor_total, valor_sinal, quantidade_sessoes,
    forma_pagamento, data_validade, proposta_aceita
) VALUES
    (7701, 7501, 7201, 'Orcamento Blackout - aprovado (FICTICIO)', DATE '2026-09-27', 'Aprovado', 4200.00, 1200.00, 5, 'PIX', DATE '2026-11-30', TRUE),
    (7702, 7502, 7202, 'Orcamento Minimalista - enviado (FICTICIO)', DATE '2026-10-10', 'Enviado', 1800.00, 500.00, 2, 'Cartao', DATE '2026-11-22', FALSE),
    (7703, 7503, 7203, 'Orcamento Dragao - fechado (FICTICIO)', DATE '2026-06-30', 'Aprovado', 5600.00, 1600.00, 4, 'Transferencia', DATE '2026-07-20', TRUE),
    (7704, 7504, 7204, 'Orcamento Floral - rascunho (FICTICIO)', DATE '2026-10-11', 'Rascunho', 2600.00, 800.00, 3, 'PIX', DATE '2026-11-29', FALSE);

-- ---------------------------
-- Financeiro (receitas e despesas)
-- ---------------------------
INSERT INTO movimentacao_financeira (
    id, projeto_id, natureza_transacao, categoria, data_ocorrencia,
    valor_monetario, forma_pagamento, status_pagamento
) VALUES
    (7801, 7501, 'ENTRADA', 'sinal', DATE '2026-09-28', 1200.00, 'PIX', 'pago'),
    (7802, 7501, 'ENTRADA', 'sessao', DATE '2026-10-18', 800.00, 'Cartao', 'pago'),
    (7803, 7501, 'ENTRADA', 'sessao', DATE '2026-11-03', 700.00, 'PIX', 'pendente'),
    (7804, 7501, 'SAIDA', 'material', DATE '2026-10-01', 250.00, 'Debito', 'pago'),
    (7805, 7502, 'ENTRADA', 'sinal', DATE '2026-11-02', 500.00, 'PIX', 'pendente'),
    (7806, 7502, 'SAIDA', 'material', DATE '2026-10-29', 120.00, 'Dinheiro', 'pendente'),
    (7807, 7503, 'ENTRADA', 'sinal', DATE '2026-07-01', 1600.00, 'PIX', 'pago'),
    (7808, 7503, 'ENTRADA', 'sessao', DATE '2026-08-20', 4000.00, 'Transferencia', 'pago'),
    (7809, 7503, 'SAIDA', 'material', DATE '2026-07-12', 430.00, 'PIX', 'pago'),
    (7810, 7504, 'SAIDA', 'deslocamento', DATE '2026-10-12', 150.00, 'PIX', 'cancelado');

-- ---------------------------
-- Follow-ups / acompanhamentos
-- ---------------------------
INSERT INTO follow_up (
    id, cliente_id, tipo_contato, status_contato,
    data_prevista, data_realizacao, descricao_observacao, proxima_acao
) VALUES
    (7901, 7202, 'WHATSAPP', 'PENDENTE', DATE '2026-10-28', NULL, 'Aguardando retorno sobre referencias visuais', 'Enviar portfolio'),
    (7902, 7202, 'LIGACAO', 'AGENDADO', DATE '2026-10-30', NULL, 'Confirmar disponibilidade para reuniao', 'Agendar visita'),
    (7903, 7201, 'WHATSAPP', 'CONCLUIDO', DATE '2026-10-09', DATE '2026-10-09', 'Cliente aprovou continuidade das sessoes', 'Acompanhar pos-sessao'),
    (7904, 7203, 'WHATSAPP', 'CONCLUIDO', DATE '2026-09-25', DATE '2026-09-25', 'Feedback final do projeto concluido', 'Solicitar depoimento'),
    (7905, 7204, 'INSTAGRAM', 'PENDENTE', DATE '2026-10-29', NULL, 'Lead pediu ajuste no conceito da arte', 'Enviar nova proposta'),
    (7906, 7204, 'WHATSAPP', 'AGENDADO', DATE '2026-11-02', NULL, 'Confirmar data para primeira sessao', 'Confirmar sinal');

-- ---------------------------
-- Agenda (atividades)
-- ---------------------------
INSERT INTO atividade (
    id, tatuador_id, projeto_id, cliente_id,
    descricao, tipo, status, titulo,
    data_atividade, hora_inicio, hora_termino,
    local, observacoes
) VALUES
    (7951, 7001, 7501, 7201, 'Sessao 3 - Blackout (FICTICIO)', 'SESSAO', 'Agendada', 'Sessao Blackout', DATE '2026-11-03', TIME '14:00:00', TIME '18:00:00', 'Estudio Centro', 'Levar stencil revisado'),
    (7952, 7001, 7502, 7202, 'Ajustar proposta minimalista (FICTICIO)', 'TAREFA', 'Pendente', 'Revisao de arte', DATE '2026-10-29', TIME '10:00:00', TIME '11:30:00', 'Home office', 'Compartilhar 3 variacoes'),
    (7953, 7001, 7504, 7204, 'Briefing floral com cliente (FICTICIO)', 'EVENTO', 'Confirmado', 'Alinhamento de escopo', DATE '2026-11-01', TIME '16:00:00', TIME '17:00:00', 'Videochamada', 'Validar tamanho e local'),
    (7954, 7001, 7503, 7203, 'Retorno de cicatrizacao (FICTICIO)', 'TAREFA', 'Concluida', 'Follow-up pos projeto', DATE '2026-09-28', TIME '11:00:00', TIME '11:30:00', 'WhatsApp', 'Sem intercorrencias'),
    (7955, 7001, NULL, NULL, 'Organizar bancada e materiais (FICTICIO)', 'ROTINA', 'Pendente', 'Rotina de abertura', DATE '2026-10-28', TIME '08:30:00', TIME '09:00:00', 'Estudio Centro', 'Checar estoque de tintas');

-- ---------------------------
-- Ajuste dos proximos IDs
-- ---------------------------
ALTER TABLE tatuador ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE preferencias_cliente ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE cliente ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE anamnese ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE contrato ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE caracteristicas_projeto ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE projeto ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE sessao ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE orcamento ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE movimentacao_financeira ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE follow_up ALTER COLUMN id RESTART WITH 8000;
ALTER TABLE atividade ALTER COLUMN id RESTART WITH 8000;
