package io.github.abbas1123.payments.dto;

import java.math.BigDecimal;

public record TransferResponse(
        Long transactionId,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BigDecimal commission,
        String status) {
}
