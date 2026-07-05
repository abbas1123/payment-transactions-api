package io.github.abbas1123.payments.exception;

/**
 * Raised when the PL/SQL transfer procedure rejects the operation
 * (insufficient funds, inactive account, unknown account, etc.).
 */
public class TransferRejectedException extends RuntimeException {

    private final String code;

    public TransferRejectedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
