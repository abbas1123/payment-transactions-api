package io.github.abbas1123.payments.web;

import io.github.abbas1123.payments.dto.TransactionResponse;
import io.github.abbas1123.payments.exception.TransactionNotFoundException;
import io.github.abbas1123.payments.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Ledger lookups")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single transaction by id")
    public TransactionResponse getTransaction(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(TransactionResponse::from)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }
}
