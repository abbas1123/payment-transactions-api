package io.github.abbas1123.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only view of a ledger entry. Rows are inserted exclusively by the
 * Oracle PL/SQL package PKG_TRANSFERS as part of the atomic transfer call.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private Long id;

    @Column(name = "from_account_id", nullable = false, updatable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false, updatable = false)
    private Long toAccountId;

    @Column(name = "amount", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "commission", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal commission;

    @Column(name = "status", nullable = false, updatable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {
        // for JPA
    }

    public Long getId() {
        return id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
