package io.github.abbas1123.payments.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String ownerName,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code") String currency,
        @NotNull @DecimalMin(value = "0.00") BigDecimal initialBalance) {
}
