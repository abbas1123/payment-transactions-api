package io.github.abbas1123.payments.dto;

import io.github.abbas1123.payments.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal commission,
        String status,
        Instant createdAt) {

    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getAmount(),
                tx.getCommission(),
                tx.getStatus(),
                tx.getCreatedAt());
    }
}
