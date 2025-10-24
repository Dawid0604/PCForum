BEGIN;

CREATE SEQUENCE IF NOT EXISTS idempotency_keys_sequence
    START WITH 1
    INCREMENT BY 1
    CACHE 20;

CREATE TABLE IF NOT EXISTS idempotency_keys(
    id                  BIGINT          NOT NULL    DEFAULT nextval('idempotency_keys_sequence'),
    idempotency_key     VARCHAR(255)    NOT NULL,
    request_hash        VARCHAR(64)     NOT NULL,
    result_data         TEXT,
    status              VARCHAR(20)     NOT NULL    DEFAULT 'PROCESSING',
    created_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP,
    expires_at          TIMESTAMP       NOT NULL,

    constraint pk_idempotency_keys
        PRIMARY KEY (id),

    constraint uk_idempotency_key
        UNIQUE (idempotency_key)
);

CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
        BEGIN
            NEW.updated_at = CURRENT_TIMESTAMP;
            RETURN NEW;
        END;
    $$ language 'plpgsql';

CREATE TRIGGER update_idempotency_keys_update_at
    BEFORE UPDATE ON idempotency_keys
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX IF NOT EXISTS idx_idempotency_key_status
    ON idempotency_keys(idempotency_key, status);

CREATE INDEX IF NOT EXISTS idx_idempotency_expires_at
    ON idempotency_keys(expires_at)
    WHERE status = 'SUCCESS';

COMMIT;