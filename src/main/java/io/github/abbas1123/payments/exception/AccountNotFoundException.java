package io.github.abbas1123.payments.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("Account not found: " + accountId);
    }

    public AccountNotFoundException(String message) {
        super(message);
    }
}
