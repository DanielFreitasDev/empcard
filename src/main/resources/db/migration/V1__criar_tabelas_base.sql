CREATE TABLE IF NOT EXISTS usuarios
(
    id            BIGSERIAL PRIMARY KEY,
    nome_usuario  VARCHAR(80)  NOT NULL UNIQUE,
    nome_exibicao VARCHAR(120) NOT NULL,
    senha         VARCHAR(255) NOT NULL,
    papel         VARCHAR(20)  NOT NULL CHECK (papel IN ('ADMIN', 'CONSULTA')),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pessoas
(
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(160)  NOT NULL,
    cpf          VARCHAR(11)   NOT NULL UNIQUE,
    logradouro   VARCHAR(160),
    numero       VARCHAR(20),
    complemento  VARCHAR(120),
    referencia   VARCHAR(120),
    bairro       VARCHAR(120),
    cidade       VARCHAR(120),
    estado       VARCHAR(2),
    cep          VARCHAR(8),
    celular      VARCHAR(11),
    whatsapp     VARCHAR(11),
    email        VARCHAR(160),
    juros_mensal NUMERIC(7, 4) NOT NULL DEFAULT 0,
    multa_atraso NUMERIC(7, 4) NOT NULL DEFAULT 0,
    ativo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cartoes
(
    id             BIGSERIAL PRIMARY KEY,
    numero         VARCHAR(16)  NOT NULL UNIQUE,
    bandeira       VARCHAR(40)  NOT NULL,
    banco          VARCHAR(120) NOT NULL,
    dia_fechamento SMALLINT     NOT NULL CHECK (dia_fechamento BETWEEN 1 AND 31),
    dia_vencimento SMALLINT     NOT NULL CHECK (dia_vencimento BETWEEN 1 AND 31),
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS lancamentos
(
    id                  BIGSERIAL PRIMARY KEY,
    pessoa_id           BIGINT         NOT NULL REFERENCES pessoas (id),
    cartao_id           BIGINT         NOT NULL REFERENCES cartoes (id),
    descricao           VARCHAR(255)   NOT NULL,
    tipo                VARCHAR(20)    NOT NULL CHECK (tipo IN ('AVULSO', 'PARCELADO', 'FIXO')),
    valor_total         NUMERIC(15, 2) NOT NULL CHECK (valor_total > 0),
    quantidade_parcelas INTEGER        NOT NULL CHECK (quantidade_parcelas > 0),
    data_compra         DATE           NOT NULL,
    data_fim_fixo       DATE,
    observacao          VARCHAR(255),
    ativo               BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pagamentos
(
    id             BIGSERIAL PRIMARY KEY,
    pessoa_id      BIGINT         NOT NULL REFERENCES pessoas (id),
    cartao_id      BIGINT         NOT NULL REFERENCES cartoes (id),
    data_pagamento DATE           NOT NULL,
    valor          NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    observacao     VARCHAR(255),
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lancamentos_pessoa_cartao ON lancamentos (pessoa_id, cartao_id);
CREATE INDEX IF NOT EXISTS idx_lancamentos_data_compra ON lancamentos (data_compra);
CREATE INDEX IF NOT EXISTS idx_pagamentos_pessoa_cartao ON pagamentos (pessoa_id, cartao_id);
CREATE INDEX IF NOT EXISTS idx_pagamentos_data_pagamento ON pagamentos (data_pagamento);

CREATE OR REPLACE FUNCTION atualizar_updated_at_coluna()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_usuarios_updated_at
    BEFORE UPDATE
    ON usuarios
    FOR EACH ROW
EXECUTE FUNCTION atualizar_updated_at_coluna();

CREATE TRIGGER trg_pessoas_updated_at
    BEFORE UPDATE
    ON pessoas
    FOR EACH ROW
EXECUTE FUNCTION atualizar_updated_at_coluna();

CREATE TRIGGER trg_cartoes_updated_at
    BEFORE UPDATE
    ON cartoes
    FOR EACH ROW
EXECUTE FUNCTION atualizar_updated_at_coluna();

CREATE TRIGGER trg_lancamentos_updated_at
    BEFORE UPDATE
    ON lancamentos
    FOR EACH ROW
EXECUTE FUNCTION atualizar_updated_at_coluna();

CREATE TRIGGER trg_pagamentos_updated_at
    BEFORE UPDATE
    ON pagamentos
    FOR EACH ROW
EXECUTE FUNCTION atualizar_updated_at_coluna();
