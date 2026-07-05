package io.github.abbas1123.payments.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long txId) {
        super("Transaction not found: " + txId);
    }
}
