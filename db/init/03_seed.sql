-- Demo accounts so the API is usable right after `docker compose up`.

ALTER SESSION SET CONTAINER = XEPDB1;

INSERT INTO payments.accounts (id, owner_name, currency, balance, status)
VALUES (payments.accounts_seq.NEXTVAL, 'Abbas Ramazanov', 'AZN', 1000.00, 'ACTIVE');

INSERT INTO payments.accounts (id, owner_name, currency, balance, status)
VALUES (payments.accounts_seq.NEXTVAL, 'Test Merchant', 'AZN', 250.00, 'ACTIVE');

INSERT INTO payments.accounts (id, owner_name, currency, balance, status)
VALUES (payments.accounts_seq.NEXTVAL, 'Frozen Account', 'AZN', 500.00, 'FROZEN');

COMMIT;
