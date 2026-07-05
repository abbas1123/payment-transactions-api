-- Business rules for money movement live in the database, next to the data,
-- so a transfer is a single atomic call: lock -> validate -> move -> record.

ALTER SESSION SET CONTAINER = XEPDB1;

CREATE OR REPLACE PACKAGE payments.pkg_transfers AS

    FUNCTION calc_commission(p_amount IN NUMBER) RETURN NUMBER;

    PROCEDURE transfer_funds(
        p_from_account IN  NUMBER,
        p_to_account   IN  NUMBER,
        p_amount       IN  NUMBER,
        o_tx_id        OUT NUMBER,
        o_status       OUT VARCHAR2);

END pkg_transfers;
/

CREATE OR REPLACE PACKAGE BODY payments.pkg_transfers AS

    c_commission_rate CONSTANT NUMBER := 0.005;  -- 0.5%
    c_commission_min  CONSTANT NUMBER := 0.10;

    FUNCTION calc_commission(p_amount IN NUMBER) RETURN NUMBER IS
    BEGIN
        RETURN GREATEST(ROUND(p_amount * c_commission_rate, 2), c_commission_min);
    END calc_commission;

    PROCEDURE transfer_funds(
        p_from_account IN  NUMBER,
        p_to_account   IN  NUMBER,
        p_amount       IN  NUMBER,
        o_tx_id        OUT NUMBER,
        o_status       OUT VARCHAR2) IS

        v_from_balance NUMBER;
        v_from_status  VARCHAR2(20);
        v_to_status    VARCHAR2(20);
        v_commission   NUMBER;
    BEGIN
        o_tx_id := NULL;

        IF p_amount IS NULL OR p_amount <= 0 THEN
            o_status := 'INVALID_AMOUNT';
            RETURN;
        END IF;

        -- Lock both rows in a deterministic order (lowest id first) to avoid
        -- deadlocks between two opposite transfers running concurrently.
        BEGIN
            FOR acc IN (SELECT id, balance, status
                          FROM payments.accounts
                         WHERE id IN (p_from_account, p_to_account)
                         ORDER BY id
                           FOR UPDATE) LOOP
                IF acc.id = p_from_account THEN
                    v_from_balance := acc.balance;
                    v_from_status  := acc.status;
                ELSE
                    v_to_status := acc.status;
                END IF;
            END LOOP;
        END;

        IF v_from_status IS NULL OR v_to_status IS NULL THEN
            o_status := 'ACCOUNT_NOT_FOUND';
            RETURN;
        END IF;

        IF v_from_status <> 'ACTIVE' OR v_to_status <> 'ACTIVE' THEN
            o_status := 'ACCOUNT_INACTIVE';
            RETURN;
        END IF;

        v_commission := calc_commission(p_amount);

        IF v_from_balance < p_amount + v_commission THEN
            o_status := 'INSUFFICIENT_FUNDS';
            RETURN;
        END IF;

        UPDATE payments.accounts
           SET balance = balance - p_amount - v_commission,
               updated_at = SYSTIMESTAMP
         WHERE id = p_from_account;

        UPDATE payments.accounts
           SET balance = balance + p_amount,
               updated_at = SYSTIMESTAMP
         WHERE id = p_to_account;

        INSERT INTO payments.transactions
            (id, from_account_id, to_account_id, amount, commission, status, created_at)
        VALUES
            (payments.transactions_seq.NEXTVAL, p_from_account, p_to_account,
             p_amount, v_commission, 'COMPLETED', SYSTIMESTAMP)
        RETURNING id INTO o_tx_id;

        o_status := 'OK';
    END transfer_funds;

END pkg_transfers;
/
