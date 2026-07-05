package io.github.abbas1123.payments.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published to Kafka after every completed transfer.
 * Consumed by the notification listener (and any future downstream service).
 */
public record TransactionEvent(
        Long transactionId,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal commission,
        String status,
        Instant occurredAt) {
}
