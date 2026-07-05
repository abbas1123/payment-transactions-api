package io.github.abbas1123.payments.dao;

import java.math.BigDecimal;

/**
 * Abstraction over the database-side transfer logic so the service layer
 * can be unit-tested without an Oracle instance.
 */
public interface TransferDao {

    TransferOutcome transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount);

    record TransferOutcome(Long transactionId, String status) {

        public boolean isOk() {
            return "OK".equals(status);
        }
    }
}
