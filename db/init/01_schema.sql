-- Executed automatically by the gvenzl/oracle-xe container on first startup.
-- Runs as SYSDBA; all objects are created in the PAYMENTS schema (see APP_USER in docker-compose.yml).

ALTER SESSION SET CONTAINER = XEPDB1;

CREATE SEQUENCE payments.accounts_seq START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE payments.transactions_seq START WITH 1000 INCREMENT BY 1 NOCACHE;

CREATE TABLE payments.accounts (
    id          NUMBER(19)     PRIMARY KEY,
    owner_name  VARCHAR2(100)  NOT NULL,
    currency    VARCHAR2(3)    DEFAULT 'AZN' NOT NULL,
    balance     NUMBER(19,2)   DEFAULT 0 NOT NULL,
    status      VARCHAR2(20)   DEFAULT 'ACTIVE' NOT NULL,
    created_at  TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_at  TIMESTAMP,
    CONSTRAINT chk_accounts_status CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0)
);

CREATE TABLE payments.transactions (
    id               NUMBER(19)   PRIMARY KEY,
    from_account_id  NUMBER(19)   NOT NULL,
    to_account_id    NUMBER(19)   NOT NULL,
    amount           NUMBER(19,2) NOT NULL,
    commission       NUMBER(19,2) NOT NULL,
    status           VARCHAR2(20) NOT NULL,
    created_at       TIMESTAMP    DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT fk_tx_from FOREIGN KEY (from_account_id) REFERENCES payments.accounts (id),
    CONSTRAINT fk_tx_to   FOREIGN KEY (to_account_id)   REFERENCES payments.accounts (id)
);

CREATE INDEX payments.idx_tx_from_account ON payments.transactions (from_account_id);
CREATE INDEX payments.idx_tx_to_account   ON payments.transactions (to_account_id);
CREATE INDEX payments.idx_tx_created_at   ON payments.transactions (created_at);
