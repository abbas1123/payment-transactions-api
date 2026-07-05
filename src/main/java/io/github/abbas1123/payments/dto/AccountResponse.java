package io.github.abbas1123.payments.dto;

import io.github.abbas1123.payments.domain.Account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        String ownerName,
        String currency,
        BigDecimal balance,
        String status,
        Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerName(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus().name(),
                account.getCreatedAt());
    }
}
